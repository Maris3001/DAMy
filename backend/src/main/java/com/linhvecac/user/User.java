package com.linhvecac.user;

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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String phone;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    /** Điểm còn dùng (P7 trừ khi đổi voucher). */
    @Column(name = "points_balance", nullable = false)
    private long pointsBalance = 0;

    /** Điểm tích lũy trọn đời — dùng xét hạng, không bao giờ tụt. */
    @Column(name = "lifetime_points", nullable = false)
    private long lifetimePoints = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tier tier = Tier.SILVER;

    /** Giá trị do DB sinh (DEFAULT SYSDATETIME()) — không ghi từ ứng dụng. */
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
