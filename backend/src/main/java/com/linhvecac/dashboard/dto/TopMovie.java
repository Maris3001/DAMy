package com.linhvecac.dashboard.dto;

/** Một phim trong bảng xếp hạng doanh thu (top phim của dashboard admin). */
public record TopMovie(
        Long movieId,
        String title,
        String posterUrl,
        long tickets,
        long revenue) {
}
