package com.linhvecac.promotion.dto;

import com.linhvecac.promotion.Campaign;
import com.linhvecac.promotion.TriggerType;
import com.linhvecac.promotion.VoucherType;
import com.linhvecac.user.Tier;

import java.time.LocalDateTime;

public record CampaignResponse(
        Long id,
        String code,
        String name,
        String description,
        VoucherType voucherType,
        long discountValue,
        Long maxDiscountAmount,
        long minOrderAmount,
        Tier minTier,
        Integer pointsCost,
        TriggerType triggerType,
        int validDays,
        Integer quantity,
        int perUserLimit,
        boolean active,
        long issuedCount,
        LocalDateTime createdAt) {

    public static CampaignResponse from(Campaign c, long issuedCount) {
        return new CampaignResponse(
                c.getId(), c.getCode(), c.getName(), c.getDescription(),
                c.getVoucherType(), c.getDiscountValue(), c.getMaxDiscountAmount(),
                c.getMinOrderAmount(), c.getMinTier(), c.getPointsCost(),
                c.getTriggerType(), c.getValidDays(), c.getQuantity(),
                c.getPerUserLimit(), c.isActive(), issuedCount, c.getCreatedAt());
    }
}
