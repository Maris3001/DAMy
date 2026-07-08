package com.linhvecac.payment;

import java.time.LocalDateTime;

/** Dữ liệu đã bóc tách từ callback VNPay (return/IPN) sau khi verify chữ ký. */
public record VnpayCallback(
        String txnRef,
        boolean success,
        long amount,        // vnp_Amount = total × 100
        String transactionNo,
        String bankCode,
        String cardType,
        LocalDateTime paidAt,
        String responseCode) {
}
