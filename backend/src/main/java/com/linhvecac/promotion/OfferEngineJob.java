package com.linhvecac.promotion;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Cron 6h sáng mỗi ngày: sinh ưu đãi cá nhân hóa. Idempotent nên chạy lại an toàn. */
@Component
@RequiredArgsConstructor
public class OfferEngineJob {

    private static final Logger log = LoggerFactory.getLogger(OfferEngineJob.class);

    private final OfferEngine offerEngine;

    @Scheduled(cron = "0 0 6 * * *")
    public void run() {
        var summary = offerEngine.runAll();
        log.info("OfferEngine cấp {} voucher (sinh nhật={}, win-back={}, thể loại={}, lên hạng={})",
                summary.total(), summary.birthday(), summary.winback(),
                summary.genreFavorite(), summary.tierUp());
    }
}
