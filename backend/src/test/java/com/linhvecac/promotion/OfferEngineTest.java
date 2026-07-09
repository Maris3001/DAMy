package com.linhvecac.promotion;

import com.linhvecac.booking.BookingRepository;
import com.linhvecac.promotion.dto.OfferRunSummary;
import com.linhvecac.user.User;
import com.linhvecac.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfferEngineTest {

    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private UserVoucherRepository userVoucherRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private OfferEngine offerEngine;

    private Campaign birthdayCampaign() {
        Campaign c = new Campaign();
        c.setId(3L);
        c.setName("Ưu đãi sinh nhật 15%");
        c.setVoucherType(VoucherType.PERCENT);
        c.setDiscountValue(15);
        c.setTriggerType(TriggerType.BIRTHDAY);
        c.setValidDays(30);
        return c;
    }

    private User member() {
        User u = new User();
        u.setId(10L);
        return u;
    }

    @Test
    void runAll_chayLai_chiCapVoucherSinhNhat1Lan() {
        when(campaignRepository.findFirstByTriggerTypeAndActiveTrue(TriggerType.BIRTHDAY))
                .thenReturn(Optional.of(birthdayCampaign()));
        // Các rule khác không có template → trả empty (default mock) → bỏ qua.
        when(userRepository.findBirthdayMembers(anyInt())).thenReturn(List.of(member()));
        // Lần 1 chưa cấp (false) → cấp; lần 2 đã cấp (true) → bỏ qua.
        when(userVoucherRepository.existsByCampaignIdAndUserIdAndPeriodKey(anyLong(), anyLong(), any()))
                .thenReturn(false, true);
        when(userVoucherRepository.existsByCode(any())).thenReturn(false);
        when(userVoucherRepository.save(any(UserVoucher.class))).thenAnswer(inv -> inv.getArgument(0));

        OfferRunSummary first = offerEngine.runAll();
        OfferRunSummary second = offerEngine.runAll();

        assertThat(first.birthday()).isEqualTo(1);
        assertThat(first.total()).isEqualTo(1);
        assertThat(second.birthday()).isZero();
        // Chỉ 1 voucher được lưu qua 2 lần chạy (idempotent).
        verify(userVoucherRepository, times(1)).save(any(UserVoucher.class));
    }

    @Test
    void runAll_khongCoTemplate_khongCapGiHet() {
        // Không stub findFirstByTriggerTypeAndActiveTrue → mọi rule nhận Optional.empty().
        OfferRunSummary summary = offerEngine.runAll();

        assertThat(summary.total()).isZero();
        verify(userVoucherRepository, times(0)).save(any());
    }
}
