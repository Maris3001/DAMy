package com.linhvecac.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Chỉ cho phép sửa họ tên/SĐT/ngày sinh — email là định danh đăng nhập, không đổi. */
public record UpdateProfileRequest(
        @NotBlank(message = "Vui lòng nhập họ tên")
        @Size(max = 150, message = "Họ tên tối đa 150 ký tự")
        String fullName,

        @Pattern(regexp = "0\\d{9}", message = "Số điện thoại không hợp lệ (10 số, bắt đầu bằng 0)")
        String phone,

        @Past(message = "Ngày sinh phải ở quá khứ")
        LocalDate birthDate) {
}
