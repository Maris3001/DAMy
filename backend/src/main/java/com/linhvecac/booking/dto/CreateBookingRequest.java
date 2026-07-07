package com.linhvecac.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Ghế không nằm trong request — server lấy từ hold còn sống của user cho suất này. */
public record CreateBookingRequest(
        @NotNull(message = "Vui lòng chọn suất chiếu")
        Long showtimeId,

        @Valid
        List<ConcessionLine> concessions) {
}
