package com.linhvecac.catalog.cinema;

import com.linhvecac.catalog.cinema.dto.GenerateSeatsRequest;
import com.linhvecac.catalog.cinema.dto.RoomRequest;
import com.linhvecac.catalog.cinema.dto.RoomResponse;
import com.linhvecac.catalog.cinema.dto.SeatResponse;
import com.linhvecac.catalog.cinema.dto.SeatUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import java.util.List;

@RestController
@RequestMapping("/api/admin/rooms")
@RequiredArgsConstructor
public class AdminRoomController {

    private final CinemaService cinemaService;

    @GetMapping
    public List<RoomResponse> list(@RequestParam(name = "cinemaId") Long cinemaId) {
        return cinemaService.listRooms(cinemaId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse create(@Valid @RequestBody RoomRequest request) {
        return cinemaService.createRoom(request);
    }

    @PutMapping("/{id}")
    public RoomResponse update(@PathVariable("id") Long id, @Valid @RequestBody RoomRequest request) {
        return cinemaService.updateRoom(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        cinemaService.deleteRoom(id);
    }

    @GetMapping("/{id}/seats")
    public List<SeatResponse> seats(@PathVariable("id") Long id) {
        return cinemaService.listSeats(id);
    }

    @PostMapping("/{id}/seats/generate")
    public List<SeatResponse> generateSeats(@PathVariable("id") Long id,
                                            @Valid @RequestBody GenerateSeatsRequest request) {
        return cinemaService.generateSeats(id, request);
    }

    @PutMapping("/{id}/seats")
    public List<SeatResponse> updateSeats(@PathVariable("id") Long id,
                                          @Valid @RequestBody SeatUpdateRequest request) {
        return cinemaService.updateSeats(id, request);
    }
}
