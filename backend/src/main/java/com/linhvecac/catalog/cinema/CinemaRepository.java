package com.linhvecac.catalog.cinema;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CinemaRepository extends JpaRepository<Cinema, Long> {

    List<Cinema> findByRegionIdOrderByNameAsc(Long regionId);

    List<Cinema> findAllByOrderByNameAsc();
}
