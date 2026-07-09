package com.linhvecac.promotion;

/** Kiểu giảm giá của voucher — mirror CHECK ck_campaigns_type. */
public enum VoucherType {
    /** Giảm theo % subtotal, có thể chặn trần max_discount_amount. */
    PERCENT,
    /** Giảm số tiền cố định (VND). */
    FIXED
}
