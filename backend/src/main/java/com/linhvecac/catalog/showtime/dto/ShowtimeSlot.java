package com.linhvecac.catalog.showtime.dto;

import com.linhvecac.catalog.showtime.Showtime;

import java.time.LocalDateTime;

/** Một suất chiếu hiển thị cho người dùng (đã biết rạp qua nhóm cha). */
public record ShowtimeSlot(
        Long id,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        long basePrice,
        Long roomId,
        String roomName,
        String roomType) {

    public static ShowtimeSlot from(Showtime s) {
        return new ShowtimeSlot(
                s.getId(),
                s.getStartsAt(),
                s.getEndsAt(),
                s.getBasePrice(),
                s.getRoom().getId(),
                s.getRoom().getName(),
                s.getRoom().getRoomType());
    }
}
