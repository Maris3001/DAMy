package com.linhvecac.booking.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Báo giá trước khi tạo đơn — mọi con số tính ở server. Discount = 0 cho tới khi có voucher (P7). */
public record QuoteResponse(
        List<HeldSeat> seats,
        List<ConcessionQuoteLine> concessions,
        long subtotal,
        long discount,
        long total,
        LocalDateTime holdExpiresAt) {

    public record ConcessionQuoteLine(
            Long concessionId,
            String name,
            int quantity,
            long unitPrice,
            long lineTotal) {
    }
}
