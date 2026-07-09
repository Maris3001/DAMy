package com.linhvecac.promotion.dto;

import jakarta.validation.constraints.NotNull;

public record RedeemRequest(
        @NotNull(message = "Vui lòng chọn ưu đãi cần đổi")
        Long campaignId) {
}
