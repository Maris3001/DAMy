package com.linhvecac.booking;

/** Trạng thái đơn đặt vé — trùng CHECK ck_bookings_status. */
public enum BookingStatus {
    PENDING_PAYMENT, PAID, EXPIRED, CANCELLED
}
