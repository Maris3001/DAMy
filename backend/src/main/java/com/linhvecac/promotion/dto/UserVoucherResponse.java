package com.linhvecac.promotion.dto;

import com.linhvecac.promotion.Campaign;
import com.linhvecac.promotion.UserVoucher;
import com.linhvecac.promotion.UserVoucherStatus;
import com.linhvecac.promotion.VoucherType;

import java.time.LocalDateTime;

/** Một phiếu trong ví voucher của user (kèm điều kiện áp dụng để FE hiển thị/tính). */
public record UserVoucherResponse(
        Long id,
        String code,
        String name,
        String description,
        VoucherType voucherType,
        long discountValue,
        Long maxDiscountAmount,
        long minOrderAmount,
        UserVoucherStatus status,
        LocalDateTime validFrom,
        LocalDateTime validTo,
        LocalDateTime usedAt) {

    public static UserVoucherResponse from(UserVoucher v) {
        Campaign c = v.getCampaign();
        return new UserVoucherResponse(
                v.getId(), v.getCode(), c.getName(), c.getDescription(),
                c.getVoucherType(), c.getDiscountValue(), c.getMaxDiscountAmount(),
                c.getMinOrderAmount(), v.getStatus(), v.getValidFrom(), v.getValidTo(), v.getUsedAt());
    }
}
