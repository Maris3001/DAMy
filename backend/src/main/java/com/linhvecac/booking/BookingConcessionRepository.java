package com.linhvecac.booking;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingConcessionRepository extends JpaRepository<BookingConcession, Long> {

    List<BookingConcession> findByBookingId(Long bookingId);
}
