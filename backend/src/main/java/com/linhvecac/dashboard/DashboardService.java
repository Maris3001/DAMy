package com.linhvecac.dashboard;

import com.linhvecac.booking.BookingRepository;
import com.linhvecac.booking.BookingSeatRepository;
import com.linhvecac.booking.BookingStatus;
import com.linhvecac.dashboard.dto.DailyRevenue;
import com.linhvecac.dashboard.dto.DashboardResponse;
import com.linhvecac.dashboard.dto.SummaryStats;
import com.linhvecac.dashboard.dto.TierCount;
import com.linhvecac.dashboard.dto.TopMovie;
import com.linhvecac.user.Tier;
import com.linhvecac.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Số liệu tổng hợp cho dashboard admin. Doanh thu neo theo created_at của đơn PAID
 * (không có cột paidAt riêng). Chuỗi ngày được fill 0 cho ngày trống để biểu đồ liền mạch.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int DEFAULT_DAYS = 30;
    private static final int MAX_DAYS = 365;
    private static final int TOP_MOVIE_LIMIT = 5;

    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(int days) {
        int window = (days < 1) ? DEFAULT_DAYS : Math.min(days, MAX_DAYS);

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        LocalDateTime yesterdayStart = today.minusDays(1).atStartOfDay();

        SummaryStats summary = new SummaryStats(
                bookingRepository.sumRevenueBetween(todayStart, tomorrowStart),
                bookingRepository.sumRevenueBetween(yesterdayStart, todayStart),
                bookingSeatRepository.countSoldBetween(todayStart, tomorrowStart),
                bookingSeatRepository.countSoldBetween(yesterdayStart, todayStart),
                userRepository.countNewMembersBetween(todayStart, tomorrowStart),
                userRepository.countNewMembersBetween(yesterdayStart, todayStart),
                bookingRepository.countByStatus(BookingStatus.PENDING_PAYMENT));

        LocalDate from = today.minusDays(window - 1L);
        List<DailyRevenue> revenueDaily = fillMissingDays(
                mapDailyRevenue(bookingRepository.revenueByDaySince(from.atStartOfDay())), from, today);

        List<TopMovie> topMovies = bookingRepository.topMoviesByRevenue(PageRequest.of(0, TOP_MOVIE_LIMIT));

        List<TierCount> tierDistribution = fillMissingTiers(userRepository.countByTier());

        return new DashboardResponse(summary, revenueDaily, topMovies, tierDistribution);
    }

    /** Ép các row native [java.sql.Date, Number, Number] về DailyRevenue. */
    private List<DailyRevenue> mapDailyRevenue(List<Object[]> rows) {
        List<DailyRevenue> result = new ArrayList<>();
        for (Object[] row : rows) {
            LocalDate day = ((java.sql.Date) row[0]).toLocalDate();
            long revenue = ((Number) row[1]).longValue();
            long orders = ((Number) row[2]).longValue();
            result.add(new DailyRevenue(day, revenue, orders));
        }
        return result;
    }

    /** Đắp 0 cho những ngày không có đơn PAID để biểu đồ đủ [from, today] và tăng dần theo ngày. */
    private List<DailyRevenue> fillMissingDays(List<DailyRevenue> rows, LocalDate from, LocalDate to) {
        Map<LocalDate, DailyRevenue> byDay = rows.stream()
                .collect(Collectors.toMap(DailyRevenue::day, r -> r, (a, b) -> a));
        List<DailyRevenue> result = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            result.add(byDay.getOrDefault(d, new DailyRevenue(d, 0, 0)));
        }
        return result;
    }

    /** Đảm bảo đủ 3 hạng (SILVER/GOLD/PLATINUM) theo thứ tự, hạng chưa có ai = 0. */
    private List<TierCount> fillMissingTiers(List<TierCount> rows) {
        Map<Tier, Long> byTier = rows.stream()
                .collect(Collectors.toMap(TierCount::tier, TierCount::count, (a, b) -> a));
        List<TierCount> result = new ArrayList<>();
        for (Tier tier : Tier.values()) {
            result.add(new TierCount(tier, byTier.getOrDefault(tier, 0L)));
        }
        return result;
    }
}
