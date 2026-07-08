package com.linhvecac.payment.dto;

/** URL chuyển hướng sang cổng thanh toán (VNPay thật hoặc cổng giả lập). */
public record PaymentInitResponse(String payUrl, String txnRef) {
}
