package com.linhvecac.user;

import com.linhvecac.dashboard.dto.TierCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /** Thành viên (role USER) — OfferEngine duyệt để xét win-back / quà lên hạng. */
    List<User> findByRole(Role role);

    /** Thành viên có sinh nhật trong tháng — OfferEngine tặng voucher sinh nhật. */
    @Query("""
            SELECT u FROM User u
            WHERE u.role = com.linhvecac.user.Role.USER
              AND u.birthDate IS NOT NULL
              AND MONTH(u.birthDate) = :month
            """)
    List<User> findBirthdayMembers(@Param("month") int month);

    /** Danh sách thành viên cho admin (phân trang), lọc tùy chọn theo hạng. */
    Page<User> findByRole(Role role, Pageable pageable);

    Page<User> findByRoleAndTier(Role role, Tier tier, Pageable pageable);

    /** Số thành viên (role USER) đăng ký trong khoảng [from, to) — stat thành viên mới hôm nay/hôm qua. */
    @Query("""
            SELECT COUNT(u) FROM User u
            WHERE u.role = com.linhvecac.user.Role.USER
              AND u.createdAt >= :from AND u.createdAt < :to
            """)
    long countNewMembersBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** Phân bố số thành viên theo hạng — dùng cho biểu đồ donut dashboard. */
    @Query("""
            SELECT new com.linhvecac.dashboard.dto.TierCount(u.tier, COUNT(u))
            FROM User u
            WHERE u.role = com.linhvecac.user.Role.USER
            GROUP BY u.tier
            """)
    List<TierCount> countByTier();
}
