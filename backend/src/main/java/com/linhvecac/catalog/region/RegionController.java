package com.linhvecac.catalog.region;

import com.linhvecac.catalog.cinema.CinemaService;
import com.linhvecac.catalog.cinema.dto.CinemaResponse;
import com.linhvecac.catalog.region.dto.RegionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;
    private final CinemaService cinemaService;

    @GetMapping
    public List<RegionResponse> list() {
        return regionService.list();
    }

    @GetMapping("/{id}/cinemas")
    public List<CinemaResponse> cinemas(@PathVariable("id") Long id) {
        return cinemaService.listCinemas(id);
    }
}
