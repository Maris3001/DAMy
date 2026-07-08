package com.linhvecac.payment;

import com.linhvecac.booking.Booking;
import com.linhvecac.booking.BookingRepository;
import com.linhvecac.booking.BookingStatus;
import com.linhvecac.common.ApiException;
import com.linhvecac.payment.dto.PaymentInitResponse;
import com.linhvecac.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Phần dùng chung khi khởi tạo thanh toán: kiểm quyền + trạng thái đơn, tạo bản ghi Payment PENDING
 * với txn_ref = booking.code + '-' + attempt. Provider cụ thể chỉ cần dựng URL cổng.
 */
public abstract class AbstractPaymentService implements PaymentService {

    protected final BookingRepository bookingRepository;
    protected final PaymentRepository paymentRepository;

    protected AbstractPaymentService(BookingRepository bookingRepository, PaymentRepository paymentRepository) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
    }

    protected abstract PaymentProvider provider();

    /** Dựng URL chuyển hướng tới cổng thanh toán cho payment vừa tạo. */
    protected abstract String buildPayUrl(Booking booking, Payment payment, String clientIp);

    @Override
    @Transactional
    public PaymentInitResponse initiate(User user, String bookingCode, String clientIp) {
        Booking booking = bookingRepository.findByCode(bookingCode)
                .filter(b -> b.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn"));
        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new ApiException(HttpStatus.CONFLICT, "Đơn không ở trạng thái chờ thanh toán");
        }
        if (!booking.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new ApiException(HttpStatus.CONFLICT, "Đơn đã hết hạn thanh toán");
        }

        long attempt = paymentRepository.countByBookingId(booking.getId()) + 1;
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setProvider(provider());
        payment.setTxnRef(booking.getCode() + "-" + attempt);
        payment.setAmount(booking.getTotal());
        payment.setStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);

        return new PaymentInitResponse(buildPayUrl(booking, payment, clientIp), payment.getTxnRef());
    }
}
