package com.linhvecac.payment;

/** Kết quả chốt một giao dịch — dùng để dựng redirect (return) và mã RspCode (IPN). */
public record FinalizeResult(Status status, String bookingCode) {

    public enum Status {
        CONFIRMED,       // vừa chuyển đơn sang PAID
        ALREADY_DONE,    // giao dịch đã xử lý trước đó (callback gọi lại — idempotent)
        NOT_FOUND,       // không thấy txn_ref
        AMOUNT_MISMATCH, // số tiền callback khác số tiền đơn
        FAILED,          // cổng báo thất bại / user hủy
        INVALID_SIG      // sai chữ ký (chỉ ở tầng controller)
    }

    public static FinalizeResult of(Status status, String bookingCode) {
        return new FinalizeResult(status, bookingCode);
    }

    public static FinalizeResult notFound() {
        return new FinalizeResult(Status.NOT_FOUND, null);
    }

    public static FinalizeResult invalidSig() {
        return new FinalizeResult(Status.INVALID_SIG, null);
    }
}
