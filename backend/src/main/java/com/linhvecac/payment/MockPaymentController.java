package com.linhvecac.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Cổng thanh toán giả lập — thay VNPay khi payment.provider=mock. Hiển thị trang chọn
 * Thành công/Hủy, sau đó chốt giao dịch và điều hướng về trang kết quả FE như luồng thật.
 */
@RestController
@ConditionalOnProperty(name = "payment.provider", havingValue = "mock", matchIfMissing = true)
public class MockPaymentController {

    private final PaymentProcessor processor;
    private final String frontendUrl;

    public MockPaymentController(PaymentProcessor processor,
                                 @Value("${app.frontend-url}") String frontendUrl) {
        this.processor = processor;
        this.frontendUrl = frontendUrl;
    }

    @GetMapping(value = "/api/payments/mock/pay", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> pay(@RequestParam(name = "txnRef") String txnRef,
                                      @RequestParam(name = "outcome", required = false) String outcome) {
        if (outcome == null) {
            return ResponseEntity.ok(choicePage(txnRef));
        }
        FinalizeResult result = processor.finalizeMock(txnRef, "success".equals(outcome));
        String status = switch (result.status()) {
            case CONFIRMED, ALREADY_DONE -> "success";
            case FAILED -> "failed";
            default -> "invalid";
        };
        String code = result.bookingCode() != null ? result.bookingCode() : "";
        URI target = URI.create(frontendUrl + "/thanh-toan/ket-qua?code=" + code + "&status=" + status);
        return ResponseEntity.status(HttpStatus.FOUND).location(target).build();
    }

    private String choicePage(String txnRef) {
        String base = "/api/payments/mock/pay?txnRef=" + txnRef;
        return """
                <!doctype html><html lang="vi"><head><meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <title>Cổng thanh toán giả lập</title>
                <style>
                  body{font-family:system-ui,sans-serif;background:#0f1115;color:#e5e7eb;
                    display:flex;min-height:100vh;align-items:center;justify-content:center;margin:0}
                  .card{background:#1a1d23;border:1px solid #2a2e37;border-radius:12px;padding:32px;
                    max-width:360px;text-align:center}
                  h1{font-size:18px;margin:0 0 4px}p{color:#9ca3af;font-size:14px;margin:0 0 24px}
                  a{display:block;padding:12px;border-radius:8px;text-decoration:none;font-weight:600;margin-top:12px}
                  .ok{background:#e11d48;color:#fff}.cancel{background:#2a2e37;color:#e5e7eb}
                </style></head><body><div class="card">
                <h1>Cổng thanh toán giả lập</h1>
                <p>Mô phỏng thanh toán cho giao dịch <strong>%s</strong></p>
                <a class="ok" href="%s&outcome=success">Thanh toán thành công</a>
                <a class="cancel" href="%s&outcome=fail">Hủy thanh toán</a>
                </div></body></html>
                """.formatted(txnRef, base, base);
    }
}
