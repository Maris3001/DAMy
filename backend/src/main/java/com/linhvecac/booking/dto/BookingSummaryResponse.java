package com.linhvecac.booking.dto;

import com.linhvecac.booking.Booking;
import com.linhvecac.booking.BookingStatus;
import com.linhvecac.catalog.cinema.Room;
import com.linhvecac.catalog.showtime.Showtime;

import java.time.LocalDateTime;

/** Dòng tóm tắt đơn cho danh sách "Vé của tôi" — không kèm chi tiết ghế/bắp nước. */
public record BookingSummaryResponse(
        String code,
        BookingStatus status,
        Long movieId,
        String movieTitle,
        String posterUrl,
        String cinemaName,
        String roomName,
        LocalDateTime startsAt,
        int seatCount,
        long total,
        LocalDateTime createdAt) {

    public static BookingSummaryResponse from(Booking b, int seatCount) {
        Showtime showtime = b.getShowtime();
        Room room = showtime.getRoom();
        return new BookingSummaryResponse(
                b.getCode(),
                b.getStatus(),
                showtime.getMovie().getId(),
                showtime.getMovie().getTitle(),
                showtime.getMovie().getPosterUrl(),
                room.getCinema().getName(),
                room.getName(),
                showtime.getStartsAt(),
                seatCount,
                b.getTotal(),
                b.getCreatedAt());
    }
}
