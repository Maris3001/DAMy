package com.linhvecac.catalog.cinema.dto;

import com.linhvecac.catalog.cinema.SeatType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** Cập nhật loại ghế hàng loạt sau khi admin chỉnh sơ đồ. */
public record SeatUpdateRequest(
        @NotNull @Valid List<SeatTypeChange> seats) {

    public record SeatTypeChange(
            @NotNull Long seatId,
            @NotNull SeatType seatType) {
    }
}
