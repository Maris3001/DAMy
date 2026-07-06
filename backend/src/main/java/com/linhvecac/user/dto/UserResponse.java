package com.linhvecac.user.dto;

import com.linhvecac.user.Role;
import com.linhvecac.user.User;

import java.time.LocalDate;

public record UserResponse(
        Long id,
        String email,
        String fullName,
        String phone,
        LocalDate birthDate,
        Role role) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getBirthDate(),
                user.getRole());
    }
}
