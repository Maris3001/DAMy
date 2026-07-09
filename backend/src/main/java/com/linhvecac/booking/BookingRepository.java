package com.linhvecac.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByCode(String code);

    Optional<Booking> findByCode(String code);

    /** Đơn của user (mới nhất trước) kèm quan hệ để dựng danh sách "Vé của tôi" trong 1 query. */
    @Query("""
            SELECT b FROM Booking b
            JOIN FETCH b.showtime s
            JOIN FETCH s.movie
            JOIN FETCH s.room r
            JOIN FETCH r.cinema
            WHERE b.user.id = :userId
            ORDER BY b.createdAt DESC
            """)
    List<Booking> findMineWithDetails(@Param("userId") Long userId);

    /**
     * Chuyển đơn sang PAID chỉ khi đang chờ thanh toán — guard idempotency cho markPaid (P5):
     * return 0 nghĩa là đơn đã PAID (callback gọi lại) hoặc đã EXPIRED, không xử lý tiếp.
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Booking b SET b.status = com.linhvecac.booking.BookingStatus.PAID
            WHERE b.code = :code
              AND b.status = com.linhvecac.booking.BookingStatus.PENDING_PAYMENT
            """)
    int markPaidIfPending(@Param("code") String code);

    /** ID các user có đơn PAID kể từ mốc thời gian — OfferEngine dùng cho quy tắc win-back / thể loại yêu thích. */
    @Query("""
            SELECT DISTINCT b.user.id FROM Booking b
            WHERE b.status = com.linhvecac.booking.BookingStatus.PAID
              AND b.createdAt >= :since
            """)
    List<Long> findUserIdsWithPaidBookingSince(@Param("since") LocalDateTime since);

    /** Chuyển các đơn chờ thanh toán quá hạn sang EXPIRED — chạy bởi job dọn dẹp. */
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Booking b SET b.status = com.linhvecac.booking.BookingStatus.EXPIRED
            WHERE b.status = com.linhvecac.booking.BookingStatus.PENDING_PAYMENT
              AND b.expiresAt < :now
            """)
    int expirePendingBefore(@Param("now") LocalDateTime now);
}
