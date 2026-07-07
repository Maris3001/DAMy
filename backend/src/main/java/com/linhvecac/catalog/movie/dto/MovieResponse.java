package com.linhvecac.catalog.movie.dto;

import com.linhvecac.catalog.movie.Movie;
import com.linhvecac.catalog.movie.MovieStatus;

import java.time.LocalDate;
import java.util.List;

public record MovieResponse(
        Long id,
        String title,
        String description,
        int durationMin,
        List<GenreResponse> genres,
        String ageRating,
        String posterUrl,
        String backdropUrl,
        String trailerUrl,
        MovieStatus status,
        LocalDate releaseDate) {

    public static MovieResponse from(Movie m) {
        return new MovieResponse(
                m.getId(),
                m.getTitle(),
                m.getDescription(),
                m.getDurationMin(),
                m.getGenres().stream().map(GenreResponse::from).toList(),
                m.getAgeRating(),
                m.getPosterUrl(),
                m.getBackdropUrl(),
                m.getTrailerUrl(),
                m.getStatus(),
                m.getReleaseDate());
    }
}
