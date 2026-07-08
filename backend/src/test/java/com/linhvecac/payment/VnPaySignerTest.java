package com.linhvecac.payment;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class VnPaySignerTest {

    private final VnPaySigner signer = new VnPaySigner();

    @Test
    void buildQuery_sortKeyVaEncodeSpaceThanhCong() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Amount", "1000000");
        params.put("vnp_OrderInfo", "Thanh toan don LVC1");

        // Sort theo key: Amount < OrderInfo < Version; space → '+'
        assertThat(signer.buildQuery(params))
                .isEqualTo("vnp_Amount=1000000&vnp_OrderInfo=Thanh+toan+don+LVC1&vnp_Version=2.1.0");
    }

    @Test
    void buildQuery_boQuaGiaTriRong() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("a", "1");
        params.put("b", "");
        params.put("c", null);
        params.put("d", "4");

        assertThat(signer.buildQuery(params)).isEqualTo("a=1&d=4");
    }

    @Test
    void signVaVerify_khopChuKy() {
        String secret = "SANDBOXSECRET1234567890";
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_TmnCode", "ABC123");
        params.put("vnp_Amount", "48000000");
        params.put("vnp_TxnRef", "LVCAB12CD34-1");
        params.put("vnp_OrderInfo", "Thanh toan don LVCAB12CD34");

        String hash = signer.hmacSha512(secret, signer.buildQuery(params));
        Map<String, String> callback = new HashMap<>(params);
        callback.put("vnp_SecureHash", hash);
        callback.put("vnp_SecureHashType", "SHA512");

        assertThat(signer.verify(callback, secret)).isTrue();
    }

    @Test
    void verify_khiBiSuaThamSo_traFalse() {
        String secret = "SANDBOXSECRET1234567890";
        Map<String, String> params = new LinkedHashMap<>();
        params.put("vnp_Amount", "48000000");
        params.put("vnp_TxnRef", "LVCAB12CD34-1");

        String hash = signer.hmacSha512(secret, signer.buildQuery(params));
        Map<String, String> callback = new HashMap<>(params);
        callback.put("vnp_SecureHash", hash);
        callback.put("vnp_Amount", "99"); // kẻ gian sửa số tiền

        assertThat(signer.verify(callback, secret)).isFalse();
    }

    @Test
    void verify_khiThieuChuKy_traFalse() {
        Map<String, String> params = new HashMap<>();
        params.put("vnp_Amount", "48000000");

        assertThat(signer.verify(params, "secret")).isFalse();
    }

    @Test
    void hmacSha512_traChuoiHex128KyTuOnDinh() {
        String a = signer.hmacSha512("secret", "vnp_Amount=1000&vnp_TxnRef=X1");
        String b = signer.hmacSha512("secret", "vnp_Amount=1000&vnp_TxnRef=X1");

        assertThat(a).isEqualTo(b).hasSize(128).matches("[0-9a-f]{128}");
    }
}
