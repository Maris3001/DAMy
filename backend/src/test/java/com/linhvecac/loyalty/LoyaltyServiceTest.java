package com.linhvecac.loyalty;

import com.linhvecac.user.Tier;
import com.linhvecac.user.User;
import com.linhvecac.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LoyaltyServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PointTransactionRepository pointTransactionRepository;
    @Mock
    private TierHistoryRepository tierHistoryRepository;

    @InjectMocks
    private LoyaltyService loyaltyService;

    private User user() {
        User u = new User();
        u.setId(10L);
        u.setTier(Tier.SILVER);
        return u;
    }

    @Test
    void pointsFor_1DiemMoi10Nghin_lamTronXuong() {
        assertThat(LoyaltyService.pointsFor(120_000)).isEqualTo(12);
        assertThat(LoyaltyService.pointsFor(125_999)).isEqualTo(12);
        assertThat(LoyaltyService.pointsFor(9_999)).isZero();
    }

    @Test
    void tierFor_dungBienNguong() {
        assertThat(TierPolicy.tierFor(0)).isEqualTo(Tier.SILVER);
        assertThat(TierPolicy.tierFor(99)).isEqualTo(Tier.SILVER);
        assertThat(TierPolicy.tierFor(100)).isEqualTo(Tier.GOLD);
        assertThat(TierPolicy.tierFor(299)).isEqualTo(Tier.GOLD);
        assertThat(TierPolicy.tierFor(300)).isEqualTo(Tier.PLATINUM);
    }

    @Test
    void pointsToNextTier_theoLifetime() {
        assertThat(TierPolicy.pointsToNextTier(0)).isEqualTo(100);
        assertThat(TierPolicy.pointsToNextTier(90)).isEqualTo(10);
        assertThat(TierPolicy.pointsToNextTier(100)).isEqualTo(200);
        assertThat(TierPolicy.pointsToNextTier(300)).isZero();
    }

    @Test
    void awardForBooking_congDiemVaGhiSoDiem() {
        User u = user();

        loyaltyService.awardForBooking(u, 50L, 120_000);

        assertThat(u.getPointsBalance()).isEqualTo(12);
        assertThat(u.getLifetimePoints()).isEqualTo(12);
        assertThat(u.getTier()).isEqualTo(Tier.SILVER);
        verify(userRepository).save(u);

        ArgumentCaptor<PointTransaction> captor = ArgumentCaptor.forClass(PointTransaction.class);
        verify(pointTransactionRepository).save(captor.capture());
        PointTransaction tx = captor.getValue();
        assertThat(tx.getType()).isEqualTo(PointTransactionType.EARN);
        assertThat(tx.getDelta()).isEqualTo(12);
        assertThat(tx.getBalanceAfter()).isEqualTo(12);
        assertThat(tx.getUserId()).isEqualTo(10L);
        assertThat(tx.getBookingId()).isEqualTo(50L);
        // Chưa lên hạng → không ghi tier_history
        verify(tierHistoryRepository, never()).save(any());
    }

    @Test
    void awardForBooking_donQuaNho_khongTichKhongGhi() {
        User u = user();

        loyaltyService.awardForBooking(u, 50L, 5_000);

        assertThat(u.getPointsBalance()).isZero();
        assertThat(u.getLifetimePoints()).isZero();
        verify(userRepository, never()).save(any());
        verify(pointTransactionRepository, never()).save(any());
    }

    @Test
    void awardForBooking_vuotNguong_lenHangVaGhiTierHistory() {
        User u = user();
        u.setPointsBalance(90);
        u.setLifetimePoints(90);

        loyaltyService.awardForBooking(u, 51L, 120_000); // +12 → 102 ≥ 100

        assertThat(u.getLifetimePoints()).isEqualTo(102);
        assertThat(u.getTier()).isEqualTo(Tier.GOLD);

        ArgumentCaptor<TierHistory> captor = ArgumentCaptor.forClass(TierHistory.class);
        verify(tierHistoryRepository).save(captor.capture());
        TierHistory history = captor.getValue();
        assertThat(history.getUserId()).isEqualTo(10L);
        assertThat(history.getOldTier()).isEqualTo(Tier.SILVER);
        assertThat(history.getNewTier()).isEqualTo(Tier.GOLD);
        assertThat(history.getLifetimePointsAt()).isEqualTo(102);
    }
}
