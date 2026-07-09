package com.linhvecac.promotion;

import com.linhvecac.common.ApiException;
import com.linhvecac.loyalty.LoyaltyService;
import com.linhvecac.promotion.dto.AppliedVoucher;
import com.linhvecac.promotion.dto.RedeemableCampaignResponse;
import com.linhvecac.promotion.dto.UserVoucherResponse;
import com.linhvecac.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * Voucher P7 — nguồn duy nhất tính giảm giá phía server. Chống race khi 2 đơn dùng chung 1 phiếu
 * bằng guard-update reserveIfAvailable (mirror markPaidIfPending của booking).
 */
@Service
@RequiredArgsConstructor
public class VoucherService {

    private final UserVoucherRepository userVoucherRepository;
    private final CampaignRepository campaignRepository;
    private final LoyaltyService loyaltyService;

    /** Số tiền giảm của campaign cho một subtotal — hàm thuần, không đụng DB (dùng lại để hiển thị & chốt giá). */
    public static long discountFor(Campaign c, long subtotal) {
        if (subtotal < c.getMinOrderAmount()) {
            return 0;
        }
        long discount;
        if (c.getVoucherType() == VoucherType.PERCENT) {
            discount = subtotal * c.getDiscountValue() / 100;
            if (c.getMaxDiscountAmount() != null) {
                discount = Math.min(discount, c.getMaxDiscountAmount());
            }
        } else {
            discount = c.getDiscountValue();
        }
        return Math.min(discount, subtotal);
    }

    /**
     * Chọn voucher áp cho báo giá/đơn: có voucherCode → validate đúng phiếu đó; không có → tự chọn phiếu lợi nhất.
     * Trả null nếu không có phiếu áp được (FE hiển thị không giảm). Không đụng DB (readOnly).
     */
    /** Sentinel: FE gửi khi user chủ động bỏ áp voucher (khác null = "tự chọn phiếu lợi nhất"). */
    public static final String NONE = "NONE";

    @Transactional(readOnly = true)
    public AppliedVoucher resolveForQuote(User user, long subtotal, String voucherCode) {
        LocalDateTime now = LocalDateTime.now();
        if (NONE.equalsIgnoreCase(voucherCode)) {
            return null;
        }
        if (voucherCode != null && !voucherCode.isBlank()) {
            return userVoucherRepository.findByCode(voucherCode.trim())
                    .filter(v -> isEligible(v, user, subtotal, now))
                    .map(v -> toApplied(v, subtotal))
                    .orElse(null);
        }
        return findBest(user, subtotal, now);
    }

    /** Auto-apply: lọc phiếu AVAILABLE hợp lệ → chọn giảm nhiều nhất, tie-break hạn dùng sớm hơn. */
    private AppliedVoucher findBest(User user, long subtotal, LocalDateTime now) {
        return userVoucherRepository
                .findByUserIdAndStatusOrderByValidToAsc(user.getId(), UserVoucherStatus.AVAILABLE).stream()
                .filter(v -> isEligible(v, user, subtotal, now))
                .max(Comparator
                        .comparingLong((UserVoucher v) -> discountFor(v.getCampaign(), subtotal))
                        .thenComparing(v -> v.getValidTo(), Comparator.reverseOrder()))
                .map(v -> toApplied(v, subtotal))
                .orElse(null);
    }

    private boolean isEligible(UserVoucher v, User user, long subtotal, LocalDateTime now) {
        Campaign c = v.getCampaign();
        return v.getUserId().equals(user.getId())
                && v.getStatus() == UserVoucherStatus.AVAILABLE
                && !now.isBefore(v.getValidFrom()) && now.isBefore(v.getValidTo())
                && (c.getMinTier() == null || user.getTier().ordinal() >= c.getMinTier().ordinal())
                && subtotal >= c.getMinOrderAmount()
                && discountFor(c, subtotal) > 0;
    }

    private AppliedVoucher toApplied(UserVoucher v, long subtotal) {
        return new AppliedVoucher(v.getCode(), v.getCampaign().getName(), discountFor(v.getCampaign(), subtotal));
    }

    /** Giữ chỗ phiếu cho đơn (guard-update) — gọi trong transaction create của booking. */
    @Transactional
    public void reserve(User user, String voucherCode, Long bookingId) {
        UserVoucher v = userVoucherRepository.findByCode(voucherCode.trim())
                .filter(x -> x.getUserId().equals(user.getId()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy voucher"));
        if (userVoucherRepository.reserveIfAvailable(v.getId(), bookingId, LocalDateTime.now()) == 0) {
            throw new ApiException(HttpStatus.CONFLICT, "Voucher đã được sử dụng hoặc hết hạn");
        }
    }

    /** Đơn thanh toán thành công → phiếu đang giữ chuyển USED (gọi từ markPaid). */
    @Transactional
    public void markUsedForBooking(Long bookingId) {
        userVoucherRepository.markUsedForBooking(bookingId, LocalDateTime.now());
    }

    /** Code + tên voucher đang gắn với đơn (để hiển thị) — null nếu đơn không dùng voucher. */
    @Transactional(readOnly = true)
    public AppliedVoucher describeForBooking(Long bookingId) {
        return userVoucherRepository.findFirstByBookingId(bookingId)
                .map(v -> new AppliedVoucher(v.getCode(), v.getCampaign().getName(), 0))
                .orElse(null);
    }

    /** Job dọn: nhả phiếu của đơn EXPIRED về AVAILABLE + đánh dấu phiếu quá hạn dùng. */
    @Transactional
    public void cleanupExpired(LocalDateTime now) {
        userVoucherRepository.releaseForExpiredBookings();
        userVoucherRepository.expireOutdated(now);
    }

    /** Đổi điểm lấy voucher: kiểm điều kiện → trừ điểm → sinh phiếu AVAILABLE. */
    @Transactional
    public UserVoucherResponse redeem(User user, Long campaignId) {
        Campaign c = campaignRepository.findById(campaignId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy ưu đãi"));
        if (!c.isActive() || c.getPointsCost() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Ưu đãi này không cho phép đổi điểm");
        }
        if (c.getMinTier() != null && user.getTier().ordinal() < c.getMinTier().ordinal()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Ưu đãi dành cho hạng " + c.getMinTier() + " trở lên");
        }
        if (c.getQuantity() != null && userVoucherRepository.countByCampaignId(campaignId) >= c.getQuantity()) {
            throw new ApiException(HttpStatus.CONFLICT, "Ưu đãi đã hết lượt");
        }
        if (userVoucherRepository.countByCampaignIdAndUserId(campaignId, user.getId()) >= c.getPerUserLimit()) {
            throw new ApiException(HttpStatus.CONFLICT, "Bạn đã đổi ưu đãi này tối đa số lần cho phép");
        }

        // Trừ điểm (ném 400 nếu thiếu) trong cùng transaction — ledger nằm ở loyalty.
        loyaltyService.redeemPoints(user, c.getPointsCost(), "Đổi voucher: " + c.getName());

        LocalDateTime now = LocalDateTime.now();
        UserVoucher v = new UserVoucher();
        v.setCode(generateCode());
        v.setCampaign(c);
        v.setUserId(user.getId());
        v.setStatus(UserVoucherStatus.AVAILABLE);
        v.setValidFrom(now);
        v.setValidTo(now.plusDays(c.getValidDays()));
        return UserVoucherResponse.from(userVoucherRepository.save(v));
    }

    /** Ví voucher của user. */
    @Transactional(readOnly = true)
    public List<UserVoucherResponse> listMine(User user) {
        return userVoucherRepository.findByUserIdOrderByCreatedAtDescIdDesc(user.getId()).stream()
                .map(UserVoucherResponse::from).toList();
    }

    /** Danh mục ưu đãi đổi điểm + trạng thái đủ điều kiện của user. */
    @Transactional(readOnly = true)
    public List<RedeemableCampaignResponse> listRedeemable(User user) {
        return campaignRepository.findByActiveTrueAndPointsCostIsNotNullOrderByPointsCostAsc().stream()
                .map(c -> RedeemableCampaignResponse.from(c, redeemEligible(c, user), redeemReason(c, user)))
                .toList();
    }

    private boolean redeemEligible(Campaign c, User user) {
        return redeemReason(c, user) == null;
    }

    private String redeemReason(Campaign c, User user) {
        if (c.getMinTier() != null && user.getTier().ordinal() < c.getMinTier().ordinal()) {
            return "Cần hạng " + c.getMinTier() + " trở lên";
        }
        if (user.getPointsBalance() < c.getPointsCost()) {
            return "Chưa đủ điểm";
        }
        if (c.getQuantity() != null && userVoucherRepository.countByCampaignId(c.getId()) >= c.getQuantity()) {
            return "Đã hết lượt";
        }
        if (userVoucherRepository.countByCampaignIdAndUserId(c.getId(), user.getId()) >= c.getPerUserLimit()) {
            return "Đã đổi tối đa số lần";
        }
        return null;
    }

    private String generateCode() {
        String code;
        do {
            code = VoucherCodes.random();
        } while (userVoucherRepository.existsByCode(code));
        return code;
    }
}
