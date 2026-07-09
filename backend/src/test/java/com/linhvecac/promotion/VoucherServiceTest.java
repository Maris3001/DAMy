package com.linhvecac.promotion;

import com.linhvecac.common.ApiException;
import com.linhvecac.loyalty.LoyaltyService;
import com.linhvecac.promotion.dto.AppliedVoucher;
import com.linhvecac.promotion.dto.UserVoucherResponse;
import com.linhvecac.user.Tier;
import com.linhvecac.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoucherServiceTest {

    @Mock
    private UserVoucherRepository userVoucherRepository;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private LoyaltyService loyaltyService;

    @InjectMocks
    private VoucherService voucherService;

    private Campaign campaign(VoucherType type, long value, Long cap, long minOrder, Tier minTier, Integer pointsCost) {
        Campaign c = new Campaign();
        c.setId(1L);
        c.setCode("C");
        c.setName("Ưu đãi test");
        c.setVoucherType(type);
        c.setDiscountValue(value);
        c.setMaxDiscountAmount(cap);
        c.setMinOrderAmount(minOrder);
        c.setMinTier(minTier);
        c.setPointsCost(pointsCost);
        c.setTriggerType(pointsCost != null ? TriggerType.REDEEM : TriggerType.MANUAL);
        c.setValidDays(30);
        c.setPerUserLimit(5);
        c.setActive(true);
        return c;
    }

    private UserVoucher voucher(String code, Campaign c) {
        UserVoucher v = new UserVoucher();
        v.setId(99L);
        v.setCode(code);
        v.setCampaign(c);
        v.setUserId(10L);
        v.setStatus(UserVoucherStatus.AVAILABLE);
        v.setValidFrom(LocalDateTime.now().minusDays(1));
        v.setValidTo(LocalDateTime.now().plusDays(10));
        return v;
    }

    private User user(Tier tier, long points) {
        User u = new User();
        u.setId(10L);
        u.setTier(tier);
        u.setPointsBalance(points);
        return u;
    }

    // ---- discountFor (hàm thuần) ----

    @Test
    void discountFor_percent_chanTranMax() {
        Campaign c = campaign(VoucherType.PERCENT, 20, 50_000L, 0, null, null);
        assertThat(VoucherService.discountFor(c, 400_000)).isEqualTo(50_000); // 20% = 80k, cap 50k
    }

    @Test
    void discountFor_percent_khongTran() {
        Campaign c = campaign(VoucherType.PERCENT, 10, null, 0, null, null);
        assertThat(VoucherService.discountFor(c, 200_000)).isEqualTo(20_000);
    }

    @Test
    void discountFor_fixed() {
        Campaign c = campaign(VoucherType.FIXED, 30_000, null, 0, null, null);
        assertThat(VoucherService.discountFor(c, 100_000)).isEqualTo(30_000);
    }

    @Test
    void discountFor_duoiMinOrder_traVe0() {
        Campaign c = campaign(VoucherType.FIXED, 20_000, null, 100_000, null, null);
        assertThat(VoucherService.discountFor(c, 90_000)).isZero();
    }

    @Test
    void discountFor_khongVuotSubtotal() {
        Campaign c = campaign(VoucherType.FIXED, 100_000, null, 0, null, null);
        assertThat(VoucherService.discountFor(c, 50_000)).isEqualTo(50_000);
    }

    // ---- auto-apply chọn phiếu lợi nhất ----

    @Test
    void resolveForQuote_tuChonPhieuGiamNhieuHon() {
        Campaign fixed = campaign(VoucherType.FIXED, 20_000, null, 0, null, null);
        Campaign percent = campaign(VoucherType.PERCENT, 10, null, 0, null, null); // 10% × 300k = 30k
        UserVoucher a = voucher("VCFIXED000", fixed);
        UserVoucher b = voucher("VCPCT00000", percent);
        when(userVoucherRepository.findByUserIdAndStatusOrderByValidToAsc(10L, UserVoucherStatus.AVAILABLE))
                .thenReturn(List.of(a, b));

        AppliedVoucher applied = voucherService.resolveForQuote(user(Tier.SILVER, 0), 300_000, null);

        assertThat(applied).isNotNull();
        assertThat(applied.code()).isEqualTo("VCPCT00000");
        assertThat(applied.discount()).isEqualTo(30_000);
    }

    @Test
    void resolveForQuote_maCuThe_duoiMinOrder_traNull() {
        Campaign c = campaign(VoucherType.FIXED, 20_000, null, 100_000, null, null);
        when(userVoucherRepository.findByCode("VCFIXED000")).thenReturn(Optional.of(voucher("VCFIXED000", c)));

        AppliedVoucher applied = voucherService.resolveForQuote(user(Tier.SILVER, 0), 90_000, "VCFIXED000");

        assertThat(applied).isNull();
    }

    // ---- reserve (guard chống race) ----

    @Test
    void reserve_khiPhieuBiChiem_nem409() {
        Campaign c = campaign(VoucherType.FIXED, 20_000, null, 0, null, null);
        when(userVoucherRepository.findByCode("VCFIXED000")).thenReturn(Optional.of(voucher("VCFIXED000", c)));
        when(userVoucherRepository.reserveIfAvailable(eq(99L), eq(77L), any())).thenReturn(0);

        assertThatThrownBy(() -> voucherService.reserve(user(Tier.SILVER, 0), "VCFIXED000", 77L))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT));
    }

    // ---- đổi điểm ----

    @Test
    void redeem_khiKhongChoDoiDiem_nem400() {
        Campaign c = campaign(VoucherType.FIXED, 20_000, null, 0, null, null); // pointsCost null
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(c));

        assertThatThrownBy(() -> voucherService.redeem(user(Tier.GOLD, 100), 1L))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));

        verify(loyaltyService, never()).redeemPoints(any(), anyLong(), any());
    }

    @Test
    void redeem_khiHangThap_nem400() {
        Campaign c = campaign(VoucherType.PERCENT, 10, 50_000L, 0, Tier.GOLD, 30);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(c));

        assertThatThrownBy(() -> voucherService.redeem(user(Tier.SILVER, 100), 1L))
                .isInstanceOf(ApiException.class)
                .satisfies(e -> assertThat(((ApiException) e).getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void redeem_hopLe_truDiemVaSinhPhieu() {
        Campaign c = campaign(VoucherType.FIXED, 20_000, null, 100_000, null, 20);
        User u = user(Tier.SILVER, 50);
        when(campaignRepository.findById(1L)).thenReturn(Optional.of(c));
        when(userVoucherRepository.countByCampaignIdAndUserId(1L, 10L)).thenReturn(0L);
        when(userVoucherRepository.existsByCode(any())).thenReturn(false);
        when(userVoucherRepository.save(any(UserVoucher.class))).thenAnswer(inv -> inv.getArgument(0));

        UserVoucherResponse response = voucherService.redeem(u, 1L);

        verify(loyaltyService).redeemPoints(eq(u), eq(20L), any());
        assertThat(response.status()).isEqualTo(UserVoucherStatus.AVAILABLE);
        assertThat(response.code()).startsWith("VC");
    }
}
