package com.linhvecac.promotion;

import com.linhvecac.booking.BookingRepository;
import com.linhvecac.promotion.dto.OfferRunSummary;
import com.linhvecac.user.Role;
import com.linhvecac.user.Tier;
import com.linhvecac.user.User;
import com.linhvecac.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Sinh ưu đãi cá nhân hóa rule-based — điểm khác biệt của đề tài. Chạy bởi cron 6h sáng và nút admin.
 * Idempotent theo (campaign, user, period_key): pre-check existsBy... + filtered unique index ux_user_vouchers_grant
 * ở DB là chốt cuối. Mỗi rule lấy campaign template theo TriggerType (bỏ qua nếu admin đã tắt).
 */
@Service
@RequiredArgsConstructor
public class OfferEngine {

    /** Ngưỡng "lâu chưa đặt vé" cho win-back (ngày). */
    private static final int WINBACK_DAYS = 60;
    /** Cửa sổ xét thể loại yêu thích (ngày). */
    private static final int FAVORITE_DAYS = 90;

    private final CampaignRepository campaignRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Transactional
    public OfferRunSummary runAll() {
        LocalDateTime now = LocalDateTime.now();
        return new OfferRunSummary(
                runBirthday(now),
                runWinback(now),
                runGenreFavorite(now),
                runTierUp(now));
    }

    /** Tặng voucher sinh nhật cho thành viên có sinh nhật trong tháng. period_key = năm → 1 lần/năm. */
    private int runBirthday(LocalDateTime now) {
        Campaign c = template(TriggerType.BIRTHDAY);
        if (c == null) {
            return 0;
        }
        String periodKey = String.valueOf(now.getYear());
        int granted = 0;
        for (User u : userRepository.findBirthdayMembers(now.getMonthValue())) {
            if (grant(c, u.getId(), periodKey, now)) {
                granted++;
            }
        }
        return granted;
    }

    /** Kéo lại thành viên lâu chưa đặt vé (không PAID trong WINBACK_DAYS, tài khoản tạo trước mốc). */
    private int runWinback(LocalDateTime now) {
        Campaign c = template(TriggerType.WINBACK);
        if (c == null) {
            return 0;
        }
        LocalDateTime cutoff = now.minusDays(WINBACK_DAYS);
        String periodKey = monthKey(now);
        Set<Long> recentlyActive = new HashSet<>(bookingRepository.findUserIdsWithPaidBookingSince(cutoff));
        int granted = 0;
        for (User u : userRepository.findByRole(Role.USER)) {
            if (recentlyActive.contains(u.getId())) {
                continue;
            }
            if (u.getCreatedAt() != null && u.getCreatedAt().isAfter(cutoff)) {
                continue; // tài khoản mới, chưa phải diện win-back
            }
            if (grant(c, u.getId(), periodKey, now)) {
                granted++;
            }
        }
        return granted;
    }

    /** Ưu đãi cho thành viên có hoạt động (PAID) trong FAVORITE_DAYS. period_key = tháng. */
    private int runGenreFavorite(LocalDateTime now) {
        Campaign c = template(TriggerType.GENRE_FAVORITE);
        if (c == null) {
            return 0;
        }
        LocalDateTime since = now.minusDays(FAVORITE_DAYS);
        String periodKey = monthKey(now);
        int granted = 0;
        for (Long userId : bookingRepository.findUserIdsWithPaidBookingSince(since)) {
            if (grant(c, userId, periodKey, now)) {
                granted++;
            }
        }
        return granted;
    }

    /** Quà cho thành viên đã lên hạng (tier ≠ SILVER). period_key = tên hạng → 1 quà/hạng. */
    private int runTierUp(LocalDateTime now) {
        Campaign c = template(TriggerType.TIER_UP);
        if (c == null) {
            return 0;
        }
        int granted = 0;
        for (User u : userRepository.findByRole(Role.USER)) {
            if (u.getTier() == Tier.SILVER) {
                continue;
            }
            if (grant(c, u.getId(), u.getTier().name(), now)) {
                granted++;
            }
        }
        return granted;
    }

    private Campaign template(TriggerType type) {
        return campaignRepository.findFirstByTriggerTypeAndActiveTrue(type).orElse(null);
    }

    /** Cấp 1 voucher nếu chưa cấp cho (campaign, user, kỳ) này. Trả false khi đã có (idempotent). */
    private boolean grant(Campaign c, Long userId, String periodKey, LocalDateTime now) {
        if (userVoucherRepository.existsByCampaignIdAndUserIdAndPeriodKey(c.getId(), userId, periodKey)) {
            return false;
        }
        UserVoucher v = new UserVoucher();
        v.setCode(generateCode());
        v.setCampaign(c);
        v.setUserId(userId);
        v.setStatus(UserVoucherStatus.AVAILABLE);
        v.setValidFrom(now);
        v.setValidTo(now.plusDays(c.getValidDays()));
        v.setPeriodKey(periodKey);
        userVoucherRepository.save(v);
        return true;
    }

    private String generateCode() {
        String code;
        do {
            code = VoucherCodes.random();
        } while (userVoucherRepository.existsByCode(code));
        return code;
    }

    private static String monthKey(LocalDateTime t) {
        return String.format("%d-%02d", t.getYear(), t.getMonthValue());
    }
}
