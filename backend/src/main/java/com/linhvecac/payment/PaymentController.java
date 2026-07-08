package com.linhvecac.payment;

import com.linhvecac.payment.dto.CreatePaymentRequest;
import com.linhvecac.payment.dto.PaymentInitResponse;
import com.linhvecac.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Khởi tạo thanh toán cho đơn của user hiện tại → trả URL cổng để FE redirect. */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public PaymentInitResponse create(@AuthenticationPrincipal User user,
                                      @Valid @RequestBody CreatePaymentRequest request,
                                      HttpServletRequest servletRequest) {
        return paymentService.initiate(user, request.bookingCode(), clientIp(servletRequest));
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
