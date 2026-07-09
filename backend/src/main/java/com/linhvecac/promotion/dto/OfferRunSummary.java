package com.linhvecac.promotion.dto;

/** Kết quả một lần chạy OfferEngine — số voucher cấp mới theo từng quy tắc. */
public record OfferRunSummary(
        int birthday,
        int winback,
        int genreFavorite,
        int tierUp) {

    public int total() {
        return birthday + winback + genreFavorite + tierUp;
    }
}
