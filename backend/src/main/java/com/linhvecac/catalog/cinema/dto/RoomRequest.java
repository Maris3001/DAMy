package com.linhvecac.catalog.cinema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RoomRequest(
        @NotNull(message = "Vui lòng chọn rạp")
        Long cinemaId,

        @NotBlank(message = "Vui lòng nhập tên phòng")
        @Size(max = 50, message = "Tên phòng tối đa 50 ký tự")
        String name,

        @Pattern(regexp = "2D|3D|IMAX", message = "Loại phòng không hợp lệ")
        String roomType) {
}
