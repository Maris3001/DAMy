package com.linhvecac.dashboard;

import com.linhvecac.booking.BookingRepository;
import com.linhvecac.booking.BookingSeatRepository;
import com.linhvecac.booking.BookingStatus;
import com.linhvecac.dashboard.dto.DailyRevenue;
import com.linhvecac.dashboard.dto.DashboardResponse;
import com.linhvecac.dashboard.dto.TierCount;
import com.linhvecac.user.Tier;
import com.linhvecac.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingSeatRepository bookingSeatRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void revenueDaily_fillZeroChoNgayTrong_dungThuTuVaSoLuong() {
        LocalDate today = LocalDate.now();
        // Repo (native) chỉ trả 2 ngày có đơn (hôm nay và 2 ngày trước) — 4 ngày còn lại phải được đắp 0.
        when(bookingRepository.revenueByDaySince(any(LocalDateTime.class))).thenReturn(List.of(
                new Object[]{java.sql.Date.valueOf(today), 500_000L, 3L},
                new Object[]{java.sql.Date.valueOf(today.minusDays(2)), 200_000L, 1L}));
        when(bookingRepository.topMoviesByRevenue(any())).thenReturn(List.of());
        when(userRepository.countByTier()).thenReturn(List.of());

        DashboardResponse res = dashboardService.getDashboard(6);

        List<DailyRevenue> daily = res.revenueDaily();
        assertThat(daily).hasSize(6);
        // Tăng dần theo ngày, kết thúc ở hôm nay.
        assertThat(daily.get(0).day()).isEqualTo(today.minusDays(5));
        assertThat(daily.get(5).day()).isEqualTo(today);
        for (int i = 1; i < daily.size(); i++) {
            assertThat(daily.get(i).day()).isAfter(daily.get(i - 1).day());
        }
        // Ngày có đơn giữ nguyên số liệu, ngày trống = 0.
        assertThat(daily.get(5).revenue()).isEqualTo(500_000);
        assertThat(daily.get(3).revenue()).isEqualTo(200_000);
        assertThat(daily.get(4).revenue()).isZero();
        assertThat(daily.get(0).revenue()).isZero();
    }

    @Test
    void tierDistribution_luonDu3Hang_hangKhongCoAiLa0() {
        when(bookingRepository.revenueByDaySince(any(LocalDateTime.class))).thenReturn(List.of());
        when(bookingRepository.topMoviesByRevenue(any())).thenReturn(List.of());
        // Chỉ có SILVER và PLATINUM trong DB — GOLD phải hiện với count 0.
        when(userRepository.countByTier()).thenReturn(List.of(
                new TierCount(Tier.SILVER, 12),
                new TierCount(Tier.PLATINUM, 2)));

        DashboardResponse res = dashboardService.getDashboard(30);

        assertThat(res.tierDistribution())
                .extracting(TierCount::tier)
                .containsExactly(Tier.SILVER, Tier.GOLD, Tier.PLATINUM);
        assertThat(res.tierDistribution())
                .extracting(TierCount::count)
                .containsExactly(12L, 0L, 2L);
    }

    @Test
    void summary_mapDungTriHomNayVaHomQua() {
        when(bookingRepository.sumRevenueBetween(any(), any())).thenReturn(300_000L, 150_000L);
        when(bookingSeatRepository.countSoldBetween(any(), any())).thenReturn(4L, 2L);
        when(userRepository.countNewMembersBetween(any(), any())).thenReturn(1L, 5L);
        when(bookingRepository.countByStatus(BookingStatus.PENDING_PAYMENT)).thenReturn(7L);
        when(bookingRepository.revenueByDaySince(any(LocalDateTime.class))).thenReturn(List.of());
        when(bookingRepository.topMoviesByRevenue(any())).thenReturn(List.of());
        when(userRepository.countByTier()).thenReturn(List.of());

        var summary = dashboardService.getDashboard(30).summary();

        assertThat(summary.revenueToday()).isEqualTo(300_000);
        assertThat(summary.revenueYesterday()).isEqualTo(150_000);
        assertThat(summary.ticketsToday()).isEqualTo(4);
        assertThat(summary.ticketsYesterday()).isEqualTo(2);
        assertThat(summary.newMembersToday()).isEqualTo(1);
        assertThat(summary.newMembersYesterday()).isEqualTo(5);
        assertThat(summary.pendingBookings()).isEqualTo(7);
    }
}
