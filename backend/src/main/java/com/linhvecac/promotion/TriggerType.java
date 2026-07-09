package com.linhvecac.promotion;

/** Nguồn phát sinh voucher của campaign — mirror CHECK ck_campaigns_trigger. */
public enum TriggerType {
    /** Admin cấp/phát hành thủ công (public). */
    MANUAL,
    /** Đổi điểm lấy voucher (points_cost ≠ null). */
    REDEEM,
    /** OfferEngine: tặng trong tháng sinh nhật. */
    BIRTHDAY,
    /** OfferEngine: kéo lại thành viên lâu chưa đặt vé. */
    WINBACK,
    /** OfferEngine: ưu đãi theo thể loại phim hay xem. */
    GENRE_FAVORITE,
    /** OfferEngine: quà khi thăng hạng thành viên. */
    TIER_UP
}
