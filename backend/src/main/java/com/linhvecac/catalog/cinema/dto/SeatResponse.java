package com.linhvecac.catalog.cinema.dto;

import com.linhvecac.catalog.cinema.Seat;
import com.linhvecac.catalog.cinema.SeatType;

public record SeatResponse(Long id, String rowLabel, int colNumber, SeatType seatType) {

    public static SeatResponse from(Seat s) {
        return new SeatResponse(s.getId(), s.getRowLabel(), s.getColNumber(), s.getSeatType());
    }
}
