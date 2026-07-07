package com.linhvecac.booking.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Toàn bộ ghế user đang giữ cho suất (sau khi thêm/nhả) + hạn giữ chung. */
public record HoldResponse(
        List<HeldSeat> seats,
        LocalDateTime holdExpiresAt) {
}
