package com.linhvecac.loyalty;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

    /** Lịch sử điểm của user, mới nhất trước (id phụ để ổn định khi cùng created_at). */
    List<PointTransaction> findByUserIdOrderByCreatedAtDescIdDesc(Long userId);
}
