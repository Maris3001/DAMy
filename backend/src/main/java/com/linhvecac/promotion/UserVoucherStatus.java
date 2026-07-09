package com.linhvecac.promotion;

/** Trạng thái phiếu của người dùng — mirror CHECK ck_user_vouchers_status. */
public enum UserVoucherStatus {
    /** Sẵn sàng dùng. */
    AVAILABLE,
    /** Đã gắn vào một đơn đang chờ thanh toán (giữ chỗ). */
    RESERVED,
    /** Đã dùng cho đơn thanh toán thành công. */
    USED,
    /** Hết hạn (valid_to đã qua). */
    EXPIRED
}
