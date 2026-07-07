package com.linhvecac.booking.dto;

import com.linhvecac.booking.BookingSeat;
import com.linhvecac.catalog.cinema.SeatType;

/** Một ghế đang giữ/đã đặt của user, kèm giá đã chốt server-side. */
public record HeldSeat(
        Long seatId,
        String label,
        SeatType seatType,
        long price) {

    public static HeldSeat from(BookingSeat bs) {
        return new HeldSeat(
                bs.getSeat().getId(),
                bs.getSeat().getRowLabel() + bs.getSeat().getColNumber(),
                bs.getSeat().getSeatType(),
                bs.getPrice());
    }
}
