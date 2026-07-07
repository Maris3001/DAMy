package com.linhvecac.catalog.cinema;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CinemaChainRepository extends JpaRepository<CinemaChain, Long> {

    List<CinemaChain> findAllByOrderByNameAsc();
}
