package com.linhvecac.loyalty;

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

/** Ghi lại mỗi lần thành viên lên hạng (lifetime_points vượt ngưỡng). */
@Entity
@Table(name = "tier_history")
@Getter
@Setter
@NoArgsConstructor
public class TierHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_tier", nullable = false)
    private Tier oldTier;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_tier", nullable = false)
    private Tier newTier;

    @Column(name = "lifetime_points_at", nullable = false)
    private long lifetimePointsAt;

    /** Giá trị do DB sinh (DEFAULT SYSDATETIME()) — không ghi từ ứng dụng. */
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
