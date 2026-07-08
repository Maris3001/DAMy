package com.linhvecac.payment;

import com.linhvecac.booking.Booking;
import com.linhvecac.booking.BookingRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Cổng thanh toán giả lập (mặc định khi chưa cấu hình VNPay). payUrl trỏ về endpoint mock của
 * chính backend → hiển thị trang chọn Thành công/Hủy → finalize → redirect về FE như luồng thật.
 */
@Service
@ConditionalOnProperty(name = "payment.provider", havingValue = "mock", matchIfMissing = true)
public class MockPaymentService extends AbstractPaymentService {

    public MockPaymentService(BookingRepository bookingRepository, PaymentRepository paymentRepository) {
        super(bookingRepository, paymentRepository);
    }

    @Override
    protected PaymentProvider provider() {
        return PaymentProvider.MOCK;
    }

    @Override
    protected String buildPayUrl(Booking booking, Payment payment, String clientIp) {
        // Đường dẫn tương đối: FE điều hướng qua proxy Vite tới backend, backend 302 về trang kết quả.
        return "/api/payments/mock/pay?txnRef=" + payment.getTxnRef();
    }
}
