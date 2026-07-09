package com.linhvecac.booking.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Báo giá trước khi tạo đơn — mọi con số tính ở server. voucherCode/voucherName = phiếu đang áp (P7, null nếu không). */
public record QuoteResponse(
        List<HeldSeat> seats,
        List<ConcessionQuoteLine> concessions,
        long subtotal,
        long discount,
        long total,
        String voucherCode,
        String voucherName,
        LocalDateTime holdExpiresAt) {

    public record ConcessionQuoteLine(
            Long concessionId,
            String name,
            int quantity,
            long unitPrice,
            long lineTotal) {
    }
}
