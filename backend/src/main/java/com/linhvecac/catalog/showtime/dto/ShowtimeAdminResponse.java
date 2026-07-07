package com.linhvecac.catalog.showtime.dto;

import com.linhvecac.catalog.showtime.Showtime;
import com.linhvecac.catalog.showtime.ShowtimeStatus;

import java.time.LocalDateTime;

public record ShowtimeAdminResponse(
        Long id,
        Long movieId,
        String movieTitle,
        Long roomId,
        String roomName,
        Long cinemaId,
        String cinemaName,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        long basePrice,
        ShowtimeStatus status) {

    public static ShowtimeAdminResponse from(Showtime s) {
        return new ShowtimeAdminResponse(
                s.getId(),
                s.getMovie().getId(),
                s.getMovie().getTitle(),
                s.getRoom().getId(),
                s.getRoom().getName(),
                s.getRoom().getCinema().getId(),
                s.getRoom().getCinema().getName(),
                s.getStartsAt(),
                s.getEndsAt(),
                s.getBasePrice(),
                s.getStatus());
    }
}
