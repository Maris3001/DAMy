package com.linhvecac.payment;

import com.linhvecac.booking.Booking;
import com.linhvecac.booking.BookingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

/**
 * Cổng VNPay thật (sandbox). Build URL thanh toán ký HMAC-SHA512 theo chuẩn VNPay 2.1.0.
 * Số tiền nhân 100, giờ theo Asia/Ho_Chi_Minh, hạn thanh toán = hạn giữ ghế của đơn.
 */
@Service
@ConditionalOnProperty(name = "payment.provider", havingValue = "vnpay")
public class VnPayPaymentService extends AbstractPaymentService {

    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final VnPaySigner signer;
    private final String tmnCode;
    private final String hashSecret;
    private final String payUrl;
    private final String returnUrl;

    public VnPayPaymentService(BookingRepository bookingRepository,
                               PaymentRepository paymentRepository,
                               VnPaySigner signer,
                               @Value("${app.vnpay.tmn-code}") String tmnCode,
                               @Value("${app.vnpay.hash-secret}") String hashSecret,
                               @Value("${app.vnpay.pay-url}") String payUrl,
                               @Value("${app.vnpay.return-url}") String returnUrl) {
        super(bookingRepository, paymentRepository);
        this.signer = signer;
        this.tmnCode = tmnCode;
        this.hashSecret = hashSecret;
        this.payUrl = payUrl;
        this.returnUrl = returnUrl;
    }

    @Override
    protected PaymentProvider provider() {
        return PaymentProvider.VNPAY;
    }

    @Override
    protected String buildPayUrl(Booking booking, Payment payment, String clientIp) {
        ZonedDateTime nowVn = ZonedDateTime.now(VN_ZONE);
        ZonedDateTime expireVn = booking.getExpiresAt().atZone(ZoneId.systemDefault())
                .withZoneSameInstant(VN_ZONE);

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", String.valueOf(booking.getTotal() * 100));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", payment.getTxnRef());
        params.put("vnp_OrderInfo", "Thanh toan don " + booking.getCode());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", (clientIp != null && !clientIp.isBlank()) ? clientIp : "127.0.0.1");
        params.put("vnp_CreateDate", nowVn.format(FMT));
        params.put("vnp_ExpireDate", expireVn.format(FMT));

        return payUrl + "?" + signer.signedQuery(params, hashSecret);
    }
}
