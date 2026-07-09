package com.linhvecac.dashboard.dto;

import com.linhvecac.user.Tier;

/** Số thành viên theo từng hạng (dùng cho biểu đồ donut phân bố hạng). */
public record TierCount(
        Tier tier,
        long count) {
}
