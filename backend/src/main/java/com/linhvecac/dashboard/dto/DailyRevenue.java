package com.linhvecac.dashboard.dto;

import java.time.LocalDate;

/** Doanh thu + số đơn PAID của một ngày (dùng cho biểu đồ cột doanh thu admin). */
public record DailyRevenue(
        LocalDate day,
        long revenue,
        long orders) {
}
