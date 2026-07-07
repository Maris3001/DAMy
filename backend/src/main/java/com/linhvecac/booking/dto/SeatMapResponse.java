package com.linhvecac.booking.dto;

import com.linhvecac.catalog.cinema.SeatType;

import java.time.LocalDateTime;
import java.util.List;

/** Sơ đồ ghế của một suất: mọi ghế trong phòng + trạng thái theo góc nhìn của user hiện tại. */
public record SeatMapResponse(
        Long showtimeId,
        List<SeatItem> seats,
        LocalDateTime holdExpiresAt) {

    /** AVAILABLE: trống · MINE: mình đang giữ · HELD: người khác giữ · BOOKED: đã bán. */
    public enum SeatState { AVAILABLE, MINE, HELD, BOOKED }

    public record SeatItem(
            Long seatId,
            String rowLabel,
            int colNumber,
            SeatType seatType,
            SeatState state,
            long price) {
    }
}
