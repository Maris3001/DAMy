package com.linhvecac.catalog.movie;

import com.linhvecac.catalog.movie.dto.MovieResponse;
import com.linhvecac.catalog.showtime.ShowtimeService;
import com.linhvecac.catalog.showtime.dto.CinemaShowtimes;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;
    private final ShowtimeService showtimeService;

    @GetMapping
    public List<MovieResponse> list(@RequestParam(name = "status", required = false) MovieStatus status) {
        return movieService.listPublic(status);
    }

    @GetMapping("/{id}")
    public MovieResponse get(@PathVariable("id") Long id) {
        return movieService.get(id);
    }

    @GetMapping("/{id}/showtimes")
    public List<CinemaShowtimes> showtimes(
            @PathVariable("id") Long id,
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return showtimeService.listByMovieAndDate(id, date != null ? date : LocalDate.now());
    }
}
