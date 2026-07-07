package com.linhvecac.catalog.movie;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByStatusOrderByReleaseDateDesc(MovieStatus status);

    List<Movie> findByStatusInOrderByReleaseDateDesc(List<MovieStatus> statuses);

    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);
}
