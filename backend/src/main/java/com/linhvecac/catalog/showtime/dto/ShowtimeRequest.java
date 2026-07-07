package com.linhvecac.catalog.showtime.dto;

import com.linhvecac.catalog.showtime.ShowtimeStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record ShowtimeRequest(
        @NotNull(message = "Vui lòng chọn phim")
        Long movieId,

        @NotNull(message = "Vui lòng chọn phòng chiếu")
        Long roomId,

        @NotNull(message = "Vui lòng chọn thời gian bắt đầu")
        LocalDateTime startsAt,

        @NotNull(message = "Vui lòng nhập giá vé")
        @Positive(message = "Giá vé phải lớn hơn 0")
        Long basePrice,

        ShowtimeStatus status) {
}
