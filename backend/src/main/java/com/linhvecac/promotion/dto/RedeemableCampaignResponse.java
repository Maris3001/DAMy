package com.linhvecac.promotion.dto;

import com.linhvecac.promotion.Campaign;
import com.linhvecac.promotion.VoucherType;
import com.linhvecac.user.Tier;

/** Campaign đổi điểm hiển thị ở trang "Đổi điểm lấy voucher"; eligible + reason cho biết user đổi được chưa. */
public record RedeemableCampaignResponse(
        Long id,
        String name,
        String description,
        VoucherType voucherType,
        long discountValue,
        Long maxDiscountAmount,
        long minOrderAmount,
        Tier minTier,
        int pointsCost,
        int validDays,
        boolean eligible,
        String reason) {

    public static RedeemableCampaignResponse from(Campaign c, boolean eligible, String reason) {
        return new RedeemableCampaignResponse(
                c.getId(), c.getName(), c.getDescription(), c.getVoucherType(),
                c.getDiscountValue(), c.getMaxDiscountAmount(), c.getMinOrderAmount(),
                c.getMinTier(), c.getPointsCost(), c.getValidDays(), eligible, reason);
    }
}
