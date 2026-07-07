package com.linhvecac.catalog.showtime.dto;

import java.util.List;

/** Suất chiếu của một rạp trong ngày đã chọn, gộp theo phim — dùng cho bước 3 wizard đặt vé. */
public record MovieShowtimes(
        Long movieId,
        String title,
        String posterUrl,
        String ageRating,
        int durationMin,
        List<ShowtimeSlot> slots) {
}
