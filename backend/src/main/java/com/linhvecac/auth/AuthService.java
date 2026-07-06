package com.linhvecac.auth;

import com.linhvecac.auth.dto.AuthResponse;
import com.linhvecac.auth.dto.LoginRequest;
import com.linhvecac.auth.dto.RegisterRequest;
import com.linhvecac.common.ApiException;
import com.linhvecac.user.User;
import com.linhvecac.user.UserRepository;
import com.linhvecac.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email đã được đăng ký");
        }
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setPhone(request.phone());
        user.setBirthDate(request.birthDate());
        userRepository.save(user);
        // Đăng ký xong cấp token luôn — người dùng không phải đăng nhập lại
        return toAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // Message chung cho cả email không tồn tại lẫn sai mật khẩu — không lộ email đã đăng ký
        return userRepository.findByEmail(normalizeEmail(request.email()))
                .filter(user -> passwordEncoder.matches(request.password(), user.getPasswordHash()))
                .map(this::toAuthResponse)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Email hoặc mật khẩu không đúng"));
    }

    private AuthResponse toAuthResponse(User user) {
        return new AuthResponse(jwtService.generateToken(user), UserResponse.from(user));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
