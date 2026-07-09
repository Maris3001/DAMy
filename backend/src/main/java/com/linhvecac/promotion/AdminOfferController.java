package com.linhvecac.promotion;

import com.linhvecac.promotion.dto.OfferRunSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Chạy OfferEngine thủ công để demo — chặn ADMIN qua SecurityConfig (/api/admin/**). */
@RestController
@RequestMapping("/api/admin/offers")
@RequiredArgsConstructor
public class AdminOfferController {

    private final OfferEngine offerEngine;

    @PostMapping("/run")
    public OfferRunSummary run() {
        return offerEngine.runAll();
    }
}
