package com.linhvecac.booking.dto;

import com.linhvecac.booking.BookingSeat;
import com.linhvecac.catalog.cinema.SeatType;

/** Một ghế đang giữ/đã đặt của user, kèm giá đã chốt server-side; ticketCode chỉ có khi đã thanh toán. */
public record HeldSeat(
        Long seatId,
        String label,
        SeatType seatType,
        long price,
        String ticketCode) {

    public static HeldSeat from(BookingSeat bs) {
        return new HeldSeat(
                bs.getSeat().getId(),
                bs.getSeat().getRowLabel() + bs.getSeat().getColNumber(),
                bs.getSeat().getSeatType(),
                bs.getPrice(),
                bs.getTicketCode());
    }
}
