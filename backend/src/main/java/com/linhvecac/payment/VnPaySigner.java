package com.linhvecac.payment;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ký/verify chữ ký VNPay (HMAC-SHA512).
 * <p>Gotcha kinh điển: chữ ký tính trên chuỗi query ĐÃ URL-encode (US-ASCII, space → '+'),
 * đúng như sample chính thức của VNPay. Khi verify callback, servlet đã decode param nên phải
 * re-encode y hệt rồi mới so hash — không được ký trên chuỗi thô.
 */
@Component
public class VnPaySigner {

    /** Chuỗi hashData = chuỗi query: các cặp key=value đã encode, sort theo key, nối bằng '&'. */
    public String buildQuery(Map<String, String> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        keys.sort(String::compareTo);
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            String value = params.get(key);
            if (value == null || value.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(encode(key)).append('=').append(encode(value));
        }
        return sb.toString();
    }

    /** hashData + "&vnp_SecureHash=" + hash — dùng để build URL thanh toán. */
    public String signedQuery(Map<String, String> params, String secret) {
        String query = buildQuery(params);
        return query + "&vnp_SecureHash=" + hmacSha512(secret, query);
    }

    /** Kiểm chữ ký callback: bỏ vnp_SecureHash(+Type), rebuild hashData, so khớp. */
    public boolean verify(Map<String, String> params, String secret) {
        Map<String, String> copy = new HashMap<>(params);
        String received = copy.remove("vnp_SecureHash");
        copy.remove("vnp_SecureHashType");
        if (received == null || received.isBlank()) {
            return false;
        }
        String expected = hmacSha512(secret, buildQuery(copy));
        return expected.equalsIgnoreCase(received);
    }

    public String hmacSha512(String secret, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Không tính được chữ ký VNPay", e);
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.US_ASCII);
    }
}
