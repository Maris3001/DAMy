package com.linhvecac.loyalty.dto;

import com.linhvecac.user.Tier;

/** Tổng quan điểm/hạng cho tab "Điểm thưởng". nextTier/pointsToNextTier null-safe khi đã PLATINUM. */
public record LoyaltySummaryResponse(
        long pointsBalance,
        long lifetimePoints,
        Tier tier,
        Tier nextTier,
        long pointsToNextTier) {
}
