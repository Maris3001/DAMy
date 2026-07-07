package com.linhvecac.booking.dto;

import com.linhvecac.booking.Booking;
import com.linhvecac.booking.BookingConcession;
import com.linhvecac.booking.BookingSeat;
import com.linhvecac.booking.BookingStatus;
import com.linhvecac.catalog.cinema.Room;
import com.linhvecac.catalog.showtime.Showtime;

import java.time.LocalDateTime;
import java.util.List;

/** Đơn đặt vé đầy đủ dữ liệu hiển thị — gọi from(...) khi các quan hệ LAZY còn trong transaction. */
public record BookingResponse(
        Long id,
        String code,
        BookingStatus status,
        Long movieId,
        String movieTitle,
        String posterUrl,
        String cinemaName,
        String roomName,
        LocalDateTime startsAt,
        List<HeldSeat> seats,
        List<QuoteResponse.ConcessionQuoteLine> concessions,
        long subtotal,
        long discount,
        long total,
        LocalDateTime expiresAt) {

    public static BookingResponse from(Booking b, List<BookingSeat> seats, List<BookingConcession> concessions) {
        Showtime showtime = b.getShowtime();
        Room room = showtime.getRoom();
        return new BookingResponse(
                b.getId(),
                b.getCode(),
                b.getStatus(),
                showtime.getMovie().getId(),
                showtime.getMovie().getTitle(),
                showtime.getMovie().getPosterUrl(),
                room.getCinema().getName(),
                room.getName(),
                showtime.getStartsAt(),
                seats.stream().map(HeldSeat::from).toList(),
                concessions.stream()
                        .map(c -> new QuoteResponse.ConcessionQuoteLine(
                                c.getConcession().getId(),
                                c.getConcession().getName(),
                                c.getQuantity(),
                                c.getUnitPrice(),
                                c.getUnitPrice() * c.getQuantity()))
                        .toList(),
                b.getSubtotal(),
                b.getDiscount(),
                b.getTotal(),
                b.getExpiresAt());
    }
}
