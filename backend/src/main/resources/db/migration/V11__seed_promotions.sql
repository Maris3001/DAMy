-- V11: seed P7 — chiến dịch demo. Điểm đổi thấp (demo-friendly, đồng bộ ngưỡng hạng P6).
-- 2 campaign REDEEM (đổi điểm) + 4 campaign template cho OfferEngine (BIRTHDAY/WINBACK/GENRE_FAVORITE/TIER_UP).

INSERT INTO campaigns
    (code, name, description, voucher_type, discount_value, max_discount_amount,
     min_order_amount, min_tier, points_cost, trigger_type, valid_days, quantity, per_user_limit, is_active)
VALUES
    (N'REDEEM_FIXED_20K', N'Giảm 20.000₫',
     N'Đổi điểm lấy voucher giảm trực tiếp 20.000₫ cho đơn từ 100.000₫',
     'FIXED', 20000, NULL, 100000, NULL, 20, 'REDEEM', 30, NULL, 5, 1),

    (N'REDEEM_PCT_10', N'Giảm 10% (tối đa 50.000₫)',
     N'Đổi điểm lấy voucher giảm 10%, dành riêng cho hạng Vàng trở lên',
     'PERCENT', 10, 50000, 0, 'GOLD', 30, 'REDEEM', 30, NULL, 5, 1),

    (N'BIRTHDAY', N'Ưu đãi sinh nhật 15%',
     N'Voucher sinh nhật giảm 15% (tối đa 50.000₫) — tự động tặng trong tháng sinh nhật của bạn',
     'PERCENT', 15, 50000, 0, NULL, NULL, 'BIRTHDAY', 30, NULL, 1, 1),

    (N'WINBACK', N'Chào mừng bạn quay lại 20%',
     N'Ưu đãi 20% (tối đa 60.000₫) dành cho thành viên đã lâu chưa đặt vé',
     'PERCENT', 20, 60000, 0, NULL, NULL, 'WINBACK', 30, NULL, 1, 1),

    (N'GENRE_FAVORITE', N'Ưu đãi thể loại yêu thích 10%',
     N'Giảm 10% (tối đa 40.000₫) cho thể loại phim bạn hay xem nhất',
     'PERCENT', 10, 40000, 0, NULL, NULL, 'GENRE_FAVORITE', 30, NULL, 1, 1),

    (N'TIER_UP', N'Quà lên hạng 50.000₫',
     N'Voucher chúc mừng khi bạn thăng hạng thành viên',
     'FIXED', 50000, NULL, 0, NULL, NULL, 'TIER_UP', 60, NULL, 1, 1);
