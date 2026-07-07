package com.linhvecac.catalog.cinema.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record GenerateSeatsRequest(
        @NotNull(message = "Vui lòng nhập số hàng")
        @Positive(message = "Số hàng phải lớn hơn 0")
        @Max(value = 26, message = "Tối đa 26 hàng (A-Z)")
        Integer rows,

        @NotNull(message = "Vui lòng nhập số cột")
        @Positive(message = "Số cột phải lớn hơn 0")
        @Max(value = 50, message = "Tối đa 50 cột")
        Integer cols) {
}
