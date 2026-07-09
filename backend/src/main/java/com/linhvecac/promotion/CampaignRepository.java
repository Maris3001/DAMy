package com.linhvecac.promotion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

    Optional<Campaign> findByCode(String code);

    /** Campaign đang bật theo loại kích hoạt — OfferEngine lấy template theo TriggerType. */
    Optional<Campaign> findFirstByTriggerTypeAndActiveTrue(TriggerType triggerType);

    /** Campaign đổi điểm còn mở (points_cost ≠ null) cho trang "Đổi điểm lấy voucher". */
    List<Campaign> findByActiveTrueAndPointsCostIsNotNullOrderByPointsCostAsc();
}
