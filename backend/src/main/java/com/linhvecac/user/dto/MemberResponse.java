package com.linhvecac.user.dto;

import com.linhvecac.user.Tier;
import com.linhvecac.user.User;

import java.time.LocalDateTime;

/** Dòng thành viên trong danh sách quản trị theo hạng. */
public record MemberResponse(
        Long id,
        String email,
        String fullName,
        String phone,
        Tier tier,
        long pointsBalance,
        long lifetimePoints,
        LocalDateTime createdAt) {

    public static MemberResponse from(User u) {
        return new MemberResponse(
                u.getId(), u.getEmail(), u.getFullName(), u.getPhone(),
                u.getTier(), u.getPointsBalance(), u.getLifetimePoints(), u.getCreatedAt());
    }
}
