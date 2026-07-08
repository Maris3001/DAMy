package com.linhvecac.payment;

import com.linhvecac.payment.dto.PaymentInitResponse;
import com.linhvecac.user.User;

/** Khởi tạo thanh toán — có đúng 1 implementation active theo property payment.provider. */
public interface PaymentService {

    PaymentInitResponse initiate(User user, String bookingCode, String clientIp);
}
