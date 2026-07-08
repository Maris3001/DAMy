package com.linhvecac.loyalty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Một dòng sổ điểm: delta có dấu (+ EARN, − REDEEM), balance_after chốt số dư sau giao dịch.
 * Lưu user_id/booking_id dạng Long (không quan hệ JPA) để loyalty không phụ thuộc ngược slice booking.
 */
@Entity
@Table(name = "point_transactions")
@Getter
@Setter
@NoArgsConstructor
public class PointTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "booking_id")
    private Long bookingId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PointTransactionType type;

    @Column(nullable = false)
    private long delta;

    @Column(name = "balance_after", nullable = false)
    private long balanceAfter;

    private String description;

    /** Giá trị do DB sinh (DEFAULT SYSDATETIME()) — không ghi từ ứng dụng. */
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
