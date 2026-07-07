package com.linhvecac.catalog.concession.dto;

import com.linhvecac.catalog.concession.ConcessionCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ConcessionRequest(
        @NotBlank(message = "Vui lòng nhập tên món")
        @Size(max = 120, message = "Tên món tối đa 120 ký tự")
        String name,

        @Size(max = 300, message = "Mô tả tối đa 300 ký tự")
        String description,

        @NotNull(message = "Vui lòng nhập giá")
        @PositiveOrZero(message = "Giá không hợp lệ")
        Long price,

        String imageUrl,

        @NotNull(message = "Vui lòng chọn nhóm")
        ConcessionCategory category,

        Boolean active) {
}
