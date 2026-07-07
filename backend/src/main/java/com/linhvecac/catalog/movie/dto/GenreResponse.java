package com.linhvecac.catalog.movie.dto;

import com.linhvecac.catalog.movie.Genre;

public record GenreResponse(
        Long id,
        String name) {

    public static GenreResponse from(Genre g) {
        return new GenreResponse(g.getId(), g.getName());
    }
}
