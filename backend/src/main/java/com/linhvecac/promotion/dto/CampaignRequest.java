package com.linhvecac.promotion.dto;

import com.linhvecac.promotion.TriggerType;
import com.linhvecac.promotion.VoucherType;
import com.linhvecac.user.Tier;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CampaignRequest(
        @NotBlank(message = "Vui lòng nhập mã campaign")
        @Size(max = 40, message = "Mã tối đa 40 ký tự")
        String code,

        @NotBlank(message = "Vui lòng nhập tên chương trình")
        @Size(max = 150, message = "Tên tối đa 150 ký tự")
        String name,

        @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
        String description,

        @NotNull(message = "Vui lòng chọn kiểu giảm giá")
        VoucherType voucherType,

        @NotNull(message = "Vui lòng nhập giá trị giảm")
        @Positive(message = "Giá trị giảm phải lớn hơn 0")
        Long discountValue,

        @PositiveOrZero(message = "Trần giảm không hợp lệ")
        Long maxDiscountAmount,

        @NotNull(message = "Vui lòng nhập giá trị đơn tối thiểu")
        @PositiveOrZero(message = "Giá trị đơn tối thiểu không hợp lệ")
        Long minOrderAmount,

        Tier minTier,

        @PositiveOrZero(message = "Số điểm đổi không hợp lệ")
        Integer pointsCost,

        @NotNull(message = "Vui lòng chọn nguồn phát sinh")
        TriggerType triggerType,

        @NotNull(message = "Vui lòng nhập số ngày hiệu lực")
        @Positive(message = "Số ngày hiệu lực phải lớn hơn 0")
        Integer validDays,

        @PositiveOrZero(message = "Số lượng không hợp lệ")
        Integer quantity,

        @NotNull(message = "Vui lòng nhập giới hạn mỗi người")
        @Positive(message = "Giới hạn mỗi người phải lớn hơn 0")
        Integer perUserLimit,

        Boolean active) {
}
