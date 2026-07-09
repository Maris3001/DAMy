package com.linhvecac.promotion.dto;

/** Voucher đã được áp vào báo giá/đơn — code + tên hiển thị + số tiền giảm (đã tính server-side). */
public record AppliedVoucher(String code, String name, long discount) {
}
