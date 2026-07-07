package com.linhvecac.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {

    /** Các row còn hiệu lực của suất (đã bán, hoặc hold chưa hết hạn) — để vẽ sơ đồ ghế. */
    @Query("""
            SELECT bs FROM BookingSeat bs
            WHERE bs.showtime.id = :showtimeId
              AND (bs.status = com.linhvecac.booking.BookingSeatStatus.CONFIRMED
                   OR bs.holdExpiresAt > :now)
            """)
    List<BookingSeat> findActiveByShowtime(@Param("showtimeId") Long showtimeId,
                                           @Param("now") LocalDateTime now);

    /** Hold còn sống của user cho suất, chưa gắn vào đơn nào — nguồn ghế khi quote/tạo đơn. */
    @Query("""
            SELECT bs FROM BookingSeat bs
            WHERE bs.showtime.id = :showtimeId
              AND bs.user.id = :userId
              AND bs.booking IS NULL
              AND bs.status = com.linhvecac.booking.BookingSeatStatus.HOLD
              AND bs.holdExpiresAt > :now
            """)
    List<BookingSeat> findLiveHolds(@Param("showtimeId") Long showtimeId,
                                    @Param("userId") Long userId,
                                    @Param("now") LocalDateTime now);

    /** Dọn hold hết hạn của các ghế xin giữ — chạy ngay trong transaction hold để không phải chờ job. */
    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE FROM BookingSeat bs
            WHERE bs.showtime.id = :showtimeId
              AND bs.seat.id IN :seatIds
              AND bs.status = com.linhvecac.booking.BookingSeatStatus.HOLD
              AND bs.holdExpiresAt < :now
            """)
    int deleteExpiredHoldsForSeats(@Param("showtimeId") Long showtimeId,
                                   @Param("seatIds") List<Long> seatIds,
                                   @Param("now") LocalDateTime now);

    /** Nhả ghế user tự bỏ chọn (chỉ hold của chính user, chưa gắn đơn). */
    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE FROM BookingSeat bs
            WHERE bs.showtime.id = :showtimeId
              AND bs.seat.id IN :seatIds
              AND bs.user.id = :userId
              AND bs.booking IS NULL
              AND bs.status = com.linhvecac.booking.BookingSeatStatus.HOLD
            """)
    int releaseHolds(@Param("showtimeId") Long showtimeId,
                     @Param("seatIds") List<Long> seatIds,
                     @Param("userId") Long userId);

    /** Dọn toàn bộ hold hết hạn (kể cả của đơn đã EXPIRED, vì expiry đồng bộ với đơn) — job 60s. */
    @Modifying(clearAutomatically = true)
    @Query("""
            DELETE FROM BookingSeat bs
            WHERE bs.status = com.linhvecac.booking.BookingSeatStatus.HOLD
              AND bs.holdExpiresAt < :now
            """)
    int deleteExpiredHolds(@Param("now") LocalDateTime now);
}
