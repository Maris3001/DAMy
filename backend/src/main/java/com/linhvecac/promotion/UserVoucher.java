package com.linhvecac.promotion;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Phiếu người dùng sở hữu (sinh từ Campaign). user_id/booking_id để dạng Long thuần (không quan hệ JPA)
 * để promotion không phụ thuộc ngược slice booking — giống PointTransaction ở loyalty.
 * period_key là khóa idempotency cho OfferEngine (null với voucher REDEEM/MANUAL).
 */
@Entity
@Table(name = "user_vouchers")
@Getter
@Setter
@NoArgsConstructor
public class UserVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "campaign_id", nullable = false)
    private Campaign campaign;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserVoucherStatus status = UserVoucherStatus.AVAILABLE;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_to", nullable = false)
    private LocalDateTime validTo;

    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "period_key")
    private String periodKey;

    @Column(name = "reserved_at")
    private LocalDateTime reservedAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    /** Giá trị do DB sinh (DEFAULT SYSDATETIME()) — không ghi từ ứng dụng. */
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
