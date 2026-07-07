package com.linhvecac.catalog.showtime.dto;

import java.util.List;

/** Suất chiếu của một phim tại một rạp trong ngày đã chọn. */
public record CinemaShowtimes(
        Long cinemaId,
        String cinemaName,
        String cinemaAddress,
        String chainName,
        Long regionId,
        List<ShowtimeSlot> slots) {
}
