package com.linhvecac.payment;

import com.linhvecac.booking.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Chốt giao dịch từ callback — luôn hoạt động (không phụ thuộc provider), dùng chung mock + VNPay.
 * Idempotent 2 lớp: (1) guard {@code payment.status == PENDING} tại đây; (2) guard-update
 * {@code markPaidIfPending} trong BookingService. Callback gọi lại chỉ trả ALREADY_DONE.
 */
@Component
@RequiredArgsConstructor
public class PaymentProcessor {

    private final PaymentRepository paymentRepository;
    private final BookingService bookingService;

    /** Chốt kết quả từ VNPay: kiểm số tiền, đúng thì đánh SUCCESS + markPaid, sai/hủy thì FAILED. */
    @Transactional
    public FinalizeResult finalizeVnpay(VnpayCallback cb) {
        Payment payment = paymentRepository.findByTxnRef(cb.txnRef()).orElse(null);
        if (payment == null) {
            return FinalizeResult.notFound();
        }
        String bookingCode = payment.getBooking().getCode();
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return FinalizeResult.of(FinalizeResult.Status.ALREADY_DONE, bookingCode);
        }
        if (payment.getAmount() * 100 != cb.amount()) {
            markFailed(payment, cb.responseCode());
            return FinalizeResult.of(FinalizeResult.Status.AMOUNT_MISMATCH, bookingCode);
        }
        if (cb.success()) {
            markSuccess(payment, cb.transactionNo(), cb.bankCode(), cb.cardType(),
                    cb.paidAt() != null ? cb.paidAt() : LocalDateTime.now());
            bookingService.markPaid(bookingCode);
            return FinalizeResult.of(FinalizeResult.Status.CONFIRMED, bookingCode);
        }
        markFailed(payment, cb.responseCode());
        return FinalizeResult.of(FinalizeResult.Status.FAILED, bookingCode);
    }

    /** Chốt kết quả từ cổng giả lập (mock): không kiểm chữ ký/số tiền. */
    @Transactional
    public FinalizeResult finalizeMock(String txnRef, boolean success) {
        Payment payment = paymentRepository.findByTxnRef(txnRef).orElse(null);
        if (payment == null) {
            return FinalizeResult.notFound();
        }
        String bookingCode = payment.getBooking().getCode();
        if (payment.getStatus() != PaymentStatus.PENDING) {
            return FinalizeResult.of(FinalizeResult.Status.ALREADY_DONE, bookingCode);
        }
        if (success) {
            markSuccess(payment, "MOCK" + payment.getId(), "MOCK", "MOCK", LocalDateTime.now());
            bookingService.markPaid(bookingCode);
            return FinalizeResult.of(FinalizeResult.Status.CONFIRMED, bookingCode);
        }
        markFailed(payment, "24"); // 24 = khách hàng hủy (theo bảng mã VNPay)
        return FinalizeResult.of(FinalizeResult.Status.FAILED, bookingCode);
    }

    private void markSuccess(Payment p, String transactionNo, String bankCode, String cardType,
                             LocalDateTime paidAt) {
        p.setStatus(PaymentStatus.SUCCESS);
        p.setResponseCode("00");
        p.setTransactionNo(transactionNo);
        p.setBankCode(bankCode);
        p.setCardType(cardType);
        p.setPaidAt(paidAt);
        p.setUpdatedAt(LocalDateTime.now());
        // markSuccess phải flush TRƯỚC guard-update clearAutomatically trong markPaid — Hibernate
        // auto-flush khi chạy JPQL update nên save() ở đây là đủ, không cần flush tường minh.
        paymentRepository.save(p);
    }

    private void markFailed(Payment p, String responseCode) {
        p.setStatus(PaymentStatus.FAILED);
        p.setResponseCode(responseCode);
        p.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(p);
    }
}
