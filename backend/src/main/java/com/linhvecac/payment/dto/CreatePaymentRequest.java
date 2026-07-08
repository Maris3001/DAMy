package com.linhvecac.payment.dto;

import jakarta.validation.constraints.NotBlank;

/** Yêu cầu khởi tạo thanh toán cho một đơn đã tạo (PENDING_PAYMENT). */
public record CreatePaymentRequest(
        @NotBlank(message = "Thiếu mã đơn") String bookingCode) {
}
