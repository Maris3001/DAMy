package com.linhvecac.promotion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {

    /** Ví voucher: tất cả phiếu của user, mới nhất trước. */
    List<UserVoucher> findByUserIdOrderByCreatedAtDescIdDesc(Long userId);

    /** Phiếu AVAILABLE của user (hạn gần trước) — nguồn cho auto-apply. */
    List<UserVoucher> findByUserIdAndStatusOrderByValidToAsc(Long userId, UserVoucherStatus status);

    Optional<UserVoucher> findByCode(String code);

    /** Phiếu đang gắn với đơn (RESERVED/USED) — để hiển thị voucher trên chi tiết đơn. */
    Optional<UserVoucher> findFirstByBookingId(Long bookingId);

    boolean existsByCode(String code);

    long countByCampaignId(Long campaignId);

    long countByCampaignIdAndUserId(Long campaignId, Long userId);

    /** Chốt idempotency cho OfferEngine ở tầng ứng dụng: đã cấp voucher campaign này cho user trong kỳ chưa. */
    boolean existsByCampaignIdAndUserIdAndPeriodKey(Long campaignId, Long userId, String periodKey);

    /**
     * Giữ chỗ phiếu cho đơn — guard-update chống race: chỉ đổi khi còn AVAILABLE.
     * Trả 0 nghĩa là phiếu đã bị dùng/hết hạn → service ném 409.
     * KHÔNG clearAutomatically: create() còn dùng lại các entity hold/booking đã nạp sau khi gọi reserve —
     * clear sẽ detach chúng → LazyInitializationException (gotcha giống booking slice).
     */
    @Modifying(flushAutomatically = true)
    @Query("""
            UPDATE UserVoucher v
            SET v.status = com.linhvecac.promotion.UserVoucherStatus.RESERVED,
                v.bookingId = :bookingId, v.reservedAt = :now
            WHERE v.id = :id
              AND v.status = com.linhvecac.promotion.UserVoucherStatus.AVAILABLE
            """)
    int reserveIfAvailable(@Param("id") Long id,
                           @Param("bookingId") Long bookingId,
                           @Param("now") LocalDateTime now);

    /** Đơn thanh toán thành công → phiếu đang giữ chuyển USED. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE UserVoucher v
            SET v.status = com.linhvecac.promotion.UserVoucherStatus.USED, v.usedAt = :now
            WHERE v.bookingId = :bookingId
              AND v.status = com.linhvecac.promotion.UserVoucherStatus.RESERVED
            """)
    int markUsedForBooking(@Param("bookingId") Long bookingId, @Param("now") LocalDateTime now);

    /** Đơn quá hạn (EXPIRED) → nhả phiếu đã giữ về AVAILABLE (dùng lại được). */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE UserVoucher v
            SET v.status = com.linhvecac.promotion.UserVoucherStatus.AVAILABLE,
                v.bookingId = NULL, v.reservedAt = NULL
            WHERE v.status = com.linhvecac.promotion.UserVoucherStatus.RESERVED
              AND v.bookingId IN (
                  SELECT b.id FROM Booking b
                  WHERE b.status = com.linhvecac.booking.BookingStatus.EXPIRED)
            """)
    int releaseForExpiredBookings();

    /** Phiếu AVAILABLE đã quá valid_to → EXPIRED. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE UserVoucher v
            SET v.status = com.linhvecac.promotion.UserVoucherStatus.EXPIRED
            WHERE v.status = com.linhvecac.promotion.UserVoucherStatus.AVAILABLE
              AND v.validTo < :now
            """)
    int expireOutdated(@Param("now") LocalDateTime now);
}
