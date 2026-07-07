package com.linhvecac.catalog.cinema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CinemaRequest(
        @NotNull(message = "Vui lòng chọn hãng rạp")
        Long chainId,

        @NotNull(message = "Vui lòng chọn khu vực")
        Long regionId,

        @NotBlank(message = "Vui lòng nhập tên rạp")
        @Size(max = 150, message = "Tên rạp tối đa 150 ký tự")
        String name,

        @NotBlank(message = "Vui lòng nhập địa chỉ")
        @Size(max = 300, message = "Địa chỉ tối đa 300 ký tự")
        String address) {
}
