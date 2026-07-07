package com.linhvecac.catalog.concession;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ConcessionRepository extends JpaRepository<Concession, Long> {

    List<Concession> findByActiveTrueOrderByCategoryAscNameAsc();
}
