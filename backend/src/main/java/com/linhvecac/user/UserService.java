package com.linhvecac.user;

import com.linhvecac.common.ApiException;
import com.linhvecac.common.PageResponse;
import com.linhvecac.user.dto.ChangePasswordRequest;
import com.linhvecac.user.dto.MemberResponse;
import com.linhvecac.user.dto.UpdateProfileRequest;
import com.linhvecac.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        return UserResponse.from(getUser(userId));
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getUser(userId);
        user.setFullName(request.fullName().trim());
        user.setPhone(request.phone());
        user.setBirthDate(request.birthDate());
        return UserResponse.from(userRepository.save(user));
    }

    /** Danh sách thành viên (role USER) cho admin, lọc tùy chọn theo hạng, phân trang mới-nhất-trước. */
    @Transactional(readOnly = true)
    public PageResponse<MemberResponse> adminMembers(Tier tier, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lifetimePoints"));
        Page<User> result = (tier == null)
                ? userRepository.findByRole(Role.USER, pageable)
                : userRepository.findByRoleAndTier(Role.USER, tier, pageable);
        return PageResponse.of(result, MemberResponse::from);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = getUser(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Mật khẩu hiện tại không đúng");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Mật khẩu mới phải khác mật khẩu hiện tại");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản"));
    }
}
