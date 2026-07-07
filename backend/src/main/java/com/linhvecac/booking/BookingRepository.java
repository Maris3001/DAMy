package com.linhvecac.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByCode(String code);

    /** Chuyển các đơn chờ thanh toán quá hạn sang EXPIRED — chạy bởi job dọn dẹp. */
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Booking b SET b.status = com.linhvecac.booking.BookingStatus.EXPIRED
            WHERE b.status = com.linhvecac.booking.BookingStatus.PENDING_PAYMENT
              AND b.expiresAt < :now
            """)
    int expirePendingBefore(@Param("now") LocalDateTime now);
}
