package com.linhvecac.loyalty;

import com.linhvecac.common.ApiException;
import com.linhvecac.loyalty.dto.LoyaltySummaryResponse;
import com.linhvecac.loyalty.dto.PointTransactionResponse;
import com.linhvecac.user.Tier;
import com.linhvecac.user.User;
import com.linhvecac.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Tích điểm + xét hạng — nguồn duy nhất tính điểm/hạng phía server (không tính ở frontend).
 * awardForBooking chạy trong cùng transaction markPaid nên tích đúng 1 lần/đơn (guard-update ở đó).
 */
@Service
@RequiredArgsConstructor
public class LoyaltyService {

    /** 1 điểm cho mỗi 10.000₫ chi tiêu (làm tròn xuống). */
    public static final long VND_PER_POINT = 10_000L;

    private final UserRepository userRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final TierHistoryRepository tierHistoryRepository;

    /** Điểm tích được từ một số tiền — dùng chung để hiển thị & cộng thực tế nhất quán. */
    public static long pointsFor(long total) {
        return total / VND_PER_POINT;
    }

    /**
     * Cộng điểm cho đơn vừa thanh toán + xét lên hạng. Gọi từ BookingService.markPaid (cùng transaction).
     * User là entity managed (booking.getUser()) nên set field sẽ được flush; ghi 1 dòng sổ điểm,
     * và 1 dòng tier_history nếu hạng thay đổi.
     */
    @Transactional
    public void awardForBooking(User user, Long bookingId, long total) {
        long points = pointsFor(total);
        if (points <= 0) {
            return;
        }

        user.setPointsBalance(user.getPointsBalance() + points);
        user.setLifetimePoints(user.getLifetimePoints() + points);

        Tier oldTier = user.getTier();
        Tier newTier = TierPolicy.tierFor(user.getLifetimePoints());
        if (newTier != oldTier) {
            user.setTier(newTier);
        }
        userRepository.save(user);

        PointTransaction tx = new PointTransaction();
        tx.setUserId(user.getId());
        tx.setBookingId(bookingId);
        tx.setType(PointTransactionType.EARN);
        tx.setDelta(points);
        tx.setBalanceAfter(user.getPointsBalance());
        tx.setDescription("Tích điểm từ đơn đặt vé");
        pointTransactionRepository.save(tx);

        if (newTier != oldTier) {
            TierHistory history = new TierHistory();
            history.setUserId(user.getId());
            history.setOldTier(oldTier);
            history.setNewTier(newTier);
            history.setLifetimePointsAt(user.getLifetimePoints());
            tierHistoryRepository.save(history);
        }
    }

    /**
     * Trừ điểm khi đổi voucher (P7) — chỉ đụng points_balance, KHÔNG đụng lifetime_points nên không tụt hạng.
     * Ghi 1 dòng sổ điểm REDEEM (delta âm). Ném 400 nếu số dư không đủ. Chạy trong transaction của caller.
     */
    @Transactional
    public void redeemPoints(User user, long cost, String description) {
        if (cost <= 0) {
            return;
        }
        if (user.getPointsBalance() < cost) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Số dư điểm không đủ để đổi ưu đãi này");
        }
        user.setPointsBalance(user.getPointsBalance() - cost);
        userRepository.save(user);

        PointTransaction tx = new PointTransaction();
        tx.setUserId(user.getId());
        tx.setType(PointTransactionType.REDEEM);
        tx.setDelta(-cost);
        tx.setBalanceAfter(user.getPointsBalance());
        tx.setDescription(description);
        pointTransactionRepository.save(tx);
    }

    @Transactional(readOnly = true)
    public LoyaltySummaryResponse getSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản"));
        return new LoyaltySummaryResponse(
                user.getPointsBalance(),
                user.getLifetimePoints(),
                user.getTier(),
                TierPolicy.nextTier(user.getTier()),
                TierPolicy.pointsToNextTier(user.getLifetimePoints()));
    }

    @Transactional(readOnly = true)
    public List<PointTransactionResponse> getPointHistory(Long userId) {
        return pointTransactionRepository.findByUserIdOrderByCreatedAtDescIdDesc(userId).stream()
                .map(PointTransactionResponse::from)
                .toList();
    }
}
