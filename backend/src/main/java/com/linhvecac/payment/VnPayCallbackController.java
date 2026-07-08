package com.linhvecac.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Callback VNPay. Return URL (trình duyệt user) → 302 về trang kết quả FE. IPN (server VNPay) →
 * JSON RspCode, là nguồn xác nhận chính thức. Cả hai gọi chung PaymentProcessor nên idempotent:
 * IPN không tới được localhost thì return handler vẫn finalize được, gọi lại không double-process.
 */
@RestController
@ConditionalOnProperty(name = "payment.provider", havingValue = "vnpay")
public class VnPayCallbackController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final PaymentProcessor processor;
    private final VnPaySigner signer;
    private final String hashSecret;
    private final String frontendUrl;

    public VnPayCallbackController(PaymentProcessor processor,
                                   VnPaySigner signer,
                                   @Value("${app.vnpay.hash-secret}") String hashSecret,
                                   @Value("${app.frontend-url}") String frontendUrl) {
        this.processor = processor;
        this.signer = signer;
        this.hashSecret = hashSecret;
        this.frontendUrl = frontendUrl;
    }

    /** Trình duyệt user quay về sau khi thanh toán → điều hướng tới trang kết quả FE. */
    @GetMapping("/api/payments/vnpay/return")
    public ResponseEntity<Void> vnpayReturn(@RequestParam Map<String, String> params) {
        FinalizeResult result = handle(params);
        String status = switch (result.status()) {
            case CONFIRMED, ALREADY_DONE -> "success";
            case FAILED, AMOUNT_MISMATCH -> "failed";
            case NOT_FOUND, INVALID_SIG -> "invalid";
        };
        String code = result.bookingCode() != null ? result.bookingCode() : "";
        URI target = URI.create(frontendUrl + "/thanh-toan/ket-qua?code=" + code + "&status=" + status);
        return ResponseEntity.status(HttpStatus.FOUND).location(target).build();
    }

    /** IPN từ server VNPay — nguồn chốt chính thức; trả RspCode theo chuẩn VNPay. */
    @GetMapping("/api/payments/vnpay/ipn")
    public Map<String, String> vnpayIpn(@RequestParam Map<String, String> params) {
        FinalizeResult result = handle(params);
        String rspCode = switch (result.status()) {
            case INVALID_SIG -> "97";
            case NOT_FOUND -> "01";
            case AMOUNT_MISMATCH -> "04";
            case ALREADY_DONE -> "02";
            case CONFIRMED, FAILED -> "00";
        };
        return Map.of("RspCode", rspCode, "Message", messageFor(rspCode));
    }

    private FinalizeResult handle(Map<String, String> params) {
        if (!signer.verify(params, hashSecret)) {
            return FinalizeResult.invalidSig();
        }
        long amount;
        try {
            amount = Long.parseLong(params.getOrDefault("vnp_Amount", "0"));
        } catch (NumberFormatException e) {
            amount = -1;
        }
        boolean success = "00".equals(params.get("vnp_ResponseCode"))
                && "00".equals(params.getOrDefault("vnp_TransactionStatus", "00"));
        return processor.finalizeVnpay(new VnpayCallback(
                params.get("vnp_TxnRef"),
                success,
                amount,
                params.get("vnp_TransactionNo"),
                params.get("vnp_BankCode"),
                params.get("vnp_CardType"),
                parsePayDate(params.get("vnp_PayDate")),
                params.get("vnp_ResponseCode")));
    }

    private LocalDateTime parsePayDate(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(raw, FMT);
        } catch (Exception e) {
            return null;
        }
    }

    private String messageFor(String rspCode) {
        return switch (rspCode) {
            case "00" -> "Confirm Success";
            case "01" -> "Order not Found";
            case "02" -> "Order already confirmed";
            case "04" -> "Invalid Amount";
            case "97" -> "Invalid Checksum";
            default -> "Unknown error";
        };
    }
}
