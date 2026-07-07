package com.linhvecac.catalog.showtime;

import com.linhvecac.catalog.showtime.dto.ShowtimeAdminResponse;
import com.linhvecac.catalog.showtime.dto.ShowtimeRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/showtimes")
@RequiredArgsConstructor
public class AdminShowtimeController {

    private final ShowtimeService showtimeService;

    @GetMapping
    public List<ShowtimeAdminResponse> list(
            @RequestParam(required = false) Long cinemaId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return showtimeService.adminList(cinemaId, roomId, date);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShowtimeAdminResponse create(@Valid @RequestBody ShowtimeRequest request) {
        return showtimeService.create(request);
    }

    @PutMapping("/{id}")
    public ShowtimeAdminResponse update(@PathVariable Long id, @Valid @RequestBody ShowtimeRequest request) {
        return showtimeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        showtimeService.delete(id);
    }
}
