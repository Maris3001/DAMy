package com.linhvecac.promotion;

import com.linhvecac.promotion.dto.RedeemRequest;
import com.linhvecac.promotion.dto.RedeemableCampaignResponse;
import com.linhvecac.promotion.dto.UserVoucherResponse;
import com.linhvecac.user.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Ví voucher + đổi điểm của thành viên — chỉ USER (ADMIN không đặt vé/không tích điểm). */
@RestController
@RequestMapping("/api/vouchers")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public List<UserVoucherResponse> myVouchers(@AuthenticationPrincipal User user) {
        return voucherService.listMine(user);
    }

    @GetMapping("/redeemable")
    @PreAuthorize("hasRole('USER')")
    public List<RedeemableCampaignResponse> redeemable(@AuthenticationPrincipal User user) {
        return voucherService.listRedeemable(user);
    }

    @PostMapping("/redeem")
    @PreAuthorize("hasRole('USER')")
    public UserVoucherResponse redeem(@AuthenticationPrincipal User user,
                                      @Valid @RequestBody RedeemRequest request) {
        return voucherService.redeem(user, request.campaignId());
    }
}
