package com.linhvecac.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterRequest(
        @NotBlank(message = "Vui lòng nhập họ tên")
        @Size(max = 150, message = "Họ tên tối đa 150 ký tự")
        String fullName,

        @NotBlank(message = "Vui lòng nhập email")
        @Email(message = "Email không hợp lệ")
        @Size(max = 255, message = "Email tối đa 255 ký tự")
        String email,

        // SĐT và ngày sinh không bắt buộc (ngày sinh dùng cho ưu đãi sinh nhật P7)
        @Pattern(regexp = "0\\d{9}", message = "Số điện thoại không hợp lệ (10 số, bắt đầu bằng 0)")
        String phone,

        @Past(message = "Ngày sinh phải ở quá khứ")
        LocalDate birthDate,

        @NotBlank(message = "Vui lòng nhập mật khẩu")
        @Size(min = 8, max = 72, message = "Mật khẩu từ 8 đến 72 ký tự")
        String password) {
}
