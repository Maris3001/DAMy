package com.linhvecac.promotion;

import com.linhvecac.user.Tier;
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
 * Định nghĩa một quy tắc ưu đãi. Vừa là "template" để OfferEngine sinh voucher cá nhân hóa,
 * vừa là mục "đổi điểm" (khi points_cost ≠ null), vừa là voucher phát thủ công (MANUAL).
 */
@Entity
@Table(name = "campaigns")
@Getter
@Setter
@NoArgsConstructor
public class Campaign {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "voucher_type", nullable = false)
    private VoucherType voucherType;

    /** PERCENT: phần trăm 1-100; FIXED: số tiền VND. */
    @Column(name = "discount_value", nullable = false)
    private long discountValue;

    /** Trần giảm cho PERCENT (null = không chặn). */
    @Column(name = "max_discount_amount")
    private Long maxDiscountAmount;

    @Column(name = "min_order_amount", nullable = false)
    private long minOrderAmount;

    /** Hạng tối thiểu để dùng/đổi (null = mọi hạng). */
    @Enumerated(EnumType.STRING)
    @Column(name = "min_tier")
    private Tier minTier;

    /** Chi phí điểm để đổi voucher; null = không cho đổi điểm. */
    @Column(name = "points_cost")
    private Integer pointsCost;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false)
    private TriggerType triggerType;

    /** Số ngày hiệu lực của voucher tính từ lúc cấp. */
    @Column(name = "valid_days", nullable = false)
    private int validDays = 30;

    /** Tổng số voucher tối đa campaign phát ra (null = không giới hạn). */
    private Integer quantity;

    @Column(name = "per_user_limit", nullable = false)
    private int perUserLimit = 1;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    /** Giá trị do DB sinh (DEFAULT SYSDATETIME()) — không ghi từ ứng dụng. */
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
