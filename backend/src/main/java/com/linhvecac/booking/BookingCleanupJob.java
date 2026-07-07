package com.linhvecac.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Job 60s: chuyển đơn quá hạn sang EXPIRED + xóa hold hết hạn để nhả ghế. */
@Component
@RequiredArgsConstructor
public class BookingCleanupJob {

    private final BookingService bookingService;

    @Scheduled(fixedDelay = 60_000, initialDelay = 60_000)
    public void run() {
        bookingService.cleanupExpired();
    }
}
