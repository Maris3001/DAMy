package com.linhvecac.booking;

import com.linhvecac.dashboard.dto.TopMovie;
import org.springframework.data.domain.Pageable;
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

    long countByStatus(BookingStatus status);

    /** Tổng doanh thu đơn PAID trong khoảng [from, to) — dùng cho stat hôm nay/hôm qua. */
    @Query("""
            SELECT COALESCE(SUM(b.total), 0) FROM Booking b
            WHERE b.status = com.linhvecac.booking.BookingStatus.PAID
              AND b.createdAt >= :from AND b.createdAt < :to
            """)
    long sumRevenueBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /**
     * Doanh thu + số đơn PAID nhóm theo ngày kể từ :from (mốc = created_at vì không có cột paidAt,
     * nhất quán với findUserIdsWithPaidBookingSince). Trả sparse — service tự fill 0 cho ngày trống.
     * Dùng native query (CONVERT(date, ...)) vì Hibernate không nhận CAST(... AS date) trong
     * constructor-expression JPQL. Mỗi row: [java.sql.Date day, Number revenue, Number orders].
     */
    @Query(value = """
            SELECT CONVERT(date, created_at) AS day, SUM(total) AS revenue, COUNT(*) AS orders
            FROM bookings
            WHERE status = 'PAID' AND created_at >= :from
            GROUP BY CONVERT(date, created_at)
            ORDER BY CONVERT(date, created_at)
            """, nativeQuery = true)
    List<Object[]> revenueByDaySince(@Param("from") LocalDateTime from);

    /** Top phim theo doanh thu đơn PAID (giới hạn qua Pageable). */
    @Query("""
            SELECT new com.linhvecac.dashboard.dto.TopMovie(
                       m.id, m.title, m.posterUrl, COUNT(b), SUM(b.total))
            FROM Booking b
            JOIN b.showtime s
            JOIN s.movie m
            WHERE b.status = com.linhvecac.booking.BookingStatus.PAID
            GROUP BY m.id, m.title, m.posterUrl
            ORDER BY SUM(b.total) DESC
            """)
    List<TopMovie> topMoviesByRevenue(Pageable pageable);

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
