package com.linhvecac.catalog.cinema;

import com.linhvecac.catalog.cinema.dto.ChainResponse;
import com.linhvecac.catalog.cinema.dto.CinemaRequest;
import com.linhvecac.catalog.cinema.dto.CinemaResponse;
import com.linhvecac.catalog.region.RegionService;
import com.linhvecac.catalog.region.dto.RegionResponse;
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
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminCinemaController {

    private final CinemaService cinemaService;
    private final RegionService regionService;

    @GetMapping("/regions")
    public List<RegionResponse> regions() {
        return regionService.list();
    }

    @GetMapping("/cinema-chains")
    public List<ChainResponse> chains() {
        return cinemaService.listChains();
    }

    @GetMapping("/cinemas")
    public List<CinemaResponse> cinemas(@RequestParam(required = false) Long regionId) {
        return cinemaService.listCinemas(regionId);
    }

    @PostMapping("/cinemas")
    @ResponseStatus(HttpStatus.CREATED)
    public CinemaResponse create(@Valid @RequestBody CinemaRequest request) {
        return cinemaService.createCinema(request);
    }

    @PutMapping("/cinemas/{id}")
    public CinemaResponse update(@PathVariable Long id, @Valid @RequestBody CinemaRequest request) {
        return cinemaService.updateCinema(id, request);
    }

    @DeleteMapping("/cinemas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        cinemaService.deleteCinema(id);
    }
}
