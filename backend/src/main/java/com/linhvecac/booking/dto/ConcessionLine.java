package com.linhvecac.booking.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ConcessionLine(
        @NotNull(message = "Thiếu mã món")
        Long concessionId,

        @Min(value = 1, message = "Số lượng tối thiểu là 1")
        @Max(value = 10, message = "Số lượng tối đa là 10")
        int quantity) {
}
