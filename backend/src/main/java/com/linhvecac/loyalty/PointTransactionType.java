package com.linhvecac.loyalty;

/** Loại giao dịch điểm — khớp CHECK constraint ck_point_txn_type trong V9__loyalty.sql. */
public enum PointTransactionType {
    /** Cộng điểm khi thanh toán thành công (P6). */
    EARN,
    /** Trừ điểm khi đổi voucher (P7). */
    REDEEM,
    /** Điều chỉnh thủ công. */
    ADJUST
}
