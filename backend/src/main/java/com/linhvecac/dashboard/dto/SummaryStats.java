package com.linhvecac.dashboard.dto;

/**
 * Số liệu tổng quan cho 4 stat card. Trả cả trị hôm nay lẫn hôm qua để FE tự tính ±%
 * (giữ backend đơn giản, tránh chia 0 phía server).
 */
public record SummaryStats(
        long revenueToday,
        long revenueYesterday,
        long ticketsToday,
        long ticketsYesterday,
        long newMembersToday,
        long newMembersYesterday,
        long pendingBookings) {
}
