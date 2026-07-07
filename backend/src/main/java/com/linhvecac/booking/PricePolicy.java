package com.linhvecac.booking;

import com.linhvecac.catalog.cinema.SeatType;

/**
 * Chính sách giá ghế theo loại — tính DUY NHẤT ở backend, FE chỉ hiển thị.
 * STANDARD = giá gốc suất; VIP = giá gốc + phụ thu; COUPLE (ghế đôi 2 người) = giá gốc × 2.
 */
public final class PricePolicy {

    public static final long VIP_SURCHARGE = 30_000;

    private PricePolicy() {
    }

    public static long seatPrice(long basePrice, SeatType seatType) {
        return switch (seatType) {
            case STANDARD -> basePrice;
            case VIP -> basePrice + VIP_SURCHARGE;
            case COUPLE -> basePrice * 2;
        };
    }
}
