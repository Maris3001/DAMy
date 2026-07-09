package com.linhvecac.promotion;

import java.security.SecureRandom;

/** Sinh mã voucher "VC" + 8 ký tự (bỏ ký tự dễ nhầm). Duy nhất được đảm bảo bởi uq_user_vouchers_code. */
final class VoucherCodes {

    private static final String ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private VoucherCodes() {
    }

    static String random() {
        StringBuilder sb = new StringBuilder("VC");
        for (int i = 0; i < 8; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
