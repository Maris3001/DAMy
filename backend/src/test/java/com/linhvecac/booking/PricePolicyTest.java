package com.linhvecac.booking;

import com.linhvecac.catalog.cinema.SeatType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PricePolicyTest {

    @Test
    void seatPrice_theoTungLoaiGhe() {
        assertThat(PricePolicy.seatPrice(90_000, SeatType.STANDARD)).isEqualTo(90_000);
        assertThat(PricePolicy.seatPrice(90_000, SeatType.VIP)).isEqualTo(120_000);
        assertThat(PricePolicy.seatPrice(90_000, SeatType.COUPLE)).isEqualTo(180_000);
    }
}
