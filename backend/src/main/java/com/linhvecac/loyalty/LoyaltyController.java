package com.linhvecac.loyalty;

import com.linhvecac.loyalty.dto.LoyaltySummaryResponse;
import com.linhvecac.loyalty.dto.PointTransactionResponse;
import com.linhvecac.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** API điểm thưởng của thành viên — chỉ USER (ADMIN không tích điểm, nhất quán với luồng đặt vé). */
@RestController
@RequestMapping("/api/loyalty")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    @GetMapping("/summary")
    @PreAuthorize("hasRole('USER')")
    public LoyaltySummaryResponse summary(@AuthenticationPrincipal User user) {
        return loyaltyService.getSummary(user.getId());
    }

    @GetMapping("/points-history")
    @PreAuthorize("hasRole('USER')")
    public List<PointTransactionResponse> pointsHistory(@AuthenticationPrincipal User user) {
        return loyaltyService.getPointHistory(user.getId());
    }
}
