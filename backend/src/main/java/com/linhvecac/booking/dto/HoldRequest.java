package com.linhvecac.booking.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record HoldRequest(
        @NotNull(message = "Vui lòng chọn suất chiếu")
        Long showtimeId,

        @NotEmpty(message = "Vui lòng chọn ghế")
        List<Long> seatIds) {
}
