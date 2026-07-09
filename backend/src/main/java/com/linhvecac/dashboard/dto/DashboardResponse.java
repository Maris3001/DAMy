package com.linhvecac.dashboard.dto;

import java.util.List;

/** Gói toàn bộ số liệu dashboard admin trong 1 phản hồi (FE gọi 1 lần). */
public record DashboardResponse(
        SummaryStats summary,
        List<DailyRevenue> revenueDaily,
        List<TopMovie> topMovies,
        List<TierCount> tierDistribution) {
}
