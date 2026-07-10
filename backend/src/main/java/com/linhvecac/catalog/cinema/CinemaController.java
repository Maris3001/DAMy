package com.linhvecac.catalog.cinema;

import com.linhvecac.catalog.cinema.dto.CinemaResponse;
import com.linhvecac.catalog.showtime.ShowtimeService;
import com.linhvecac.catalog.showtime.dto.MovieShowtimes;
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
@RequestMapping("/api/cinemas")
@RequiredArgsConstructor
public class CinemaController {

    private final CinemaService cinemaService;
    private final ShowtimeService showtimeService;

    @GetMapping
    public List<CinemaResponse> list(@RequestParam(name = "regionId", required = false) Long regionId) {
        return cinemaService.listCinemas(regionId);
    }

    /** Suất chiếu của rạp trong ngày, gộp theo phim — bước 3 wizard đặt vé. */
    @GetMapping("/{id}/showtimes")
    public List<MovieShowtimes> showtimes(
            @PathVariable("id") Long id,
            @RequestParam(name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return showtimeService.listByCinemaAndDate(id, date);
    }
}
