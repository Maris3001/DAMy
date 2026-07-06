package com.linhvecac.auth.dto;

import com.linhvecac.user.dto.UserResponse;

public record AuthResponse(String token, UserResponse user) {
}
