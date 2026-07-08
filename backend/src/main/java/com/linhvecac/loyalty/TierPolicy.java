package com.linhvecac.loyalty;

import com.linhvecac.user.Tier;

/**
 * Ngưỡng & tính hạng thành viên theo lifetime_points — hàm thuần, không job hạ hạng.
 * Ngưỡng đặt thấp (demo-friendly) để lên hạng tự nhiên khi mua vài vé lúc bảo vệ đồ án;
 * đổi tại đây nếu cần. Đổi điểm (P7) chỉ trừ points_balance nên không làm tụt hạng.
 */
public final class TierPolicy {

    public static final long GOLD_MIN = 100;
    public static final long PLATINUM_MIN = 300;

    private TierPolicy() {
    }

    public static Tier tierFor(long lifetimePoints) {
        if (lifetimePoints >= PLATINUM_MIN) {
            return Tier.PLATINUM;
        }
        if (lifetimePoints >= GOLD_MIN) {
            return Tier.GOLD;
        }
        return Tier.SILVER;
    }

    /** Hạng kế tiếp, null nếu đã cao nhất. */
    public static Tier nextTier(Tier tier) {
        return switch (tier) {
            case SILVER -> Tier.GOLD;
            case GOLD -> Tier.PLATINUM;
            case PLATINUM -> null;
        };
    }

    /** Số điểm còn thiếu để lên hạng kế; 0 nếu đã cao nhất. */
    public static long pointsToNextTier(long lifetimePoints) {
        if (lifetimePoints < GOLD_MIN) {
            return GOLD_MIN - lifetimePoints;
        }
        if (lifetimePoints < PLATINUM_MIN) {
            return PLATINUM_MIN - lifetimePoints;
        }
        return 0;
    }
}
