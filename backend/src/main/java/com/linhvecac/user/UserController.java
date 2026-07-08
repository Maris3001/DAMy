package com.linhvecac.user;

import com.linhvecac.user.dto.ChangePasswordRequest;
import com.linhvecac.user.dto.UpdateProfileRequest;
import com.linhvecac.user.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal User user) {
        return userService.getProfile(user.getId());
    }

    @PutMapping("/me")
    public UserResponse updateMe(@AuthenticationPrincipal User user,
                                 @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(user.getId(), request);
    }

    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal User user,
                               @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(user.getId(), request);
    }
}
