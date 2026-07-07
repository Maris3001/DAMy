package com.linhvecac.catalog.cinema.dto;

import com.linhvecac.catalog.cinema.Room;

public record RoomResponse(
        Long id,
        Long cinemaId,
        String cinemaName,
        String name,
        String roomType,
        long seatCount) {

    public static RoomResponse from(Room r, long seatCount) {
        return new RoomResponse(
                r.getId(),
                r.getCinema().getId(),
                r.getCinema().getName(),
                r.getName(),
                r.getRoomType(),
                seatCount);
    }
}
