package com.linhvecac.payment;

/** Trạng thái giao dịch — mirror CHECK ck_payments_status ở DB. */
public enum PaymentStatus {
    PENDING, SUCCESS, FAILED
}
