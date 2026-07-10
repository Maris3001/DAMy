package com.linhvecac.user;

import com.linhvecac.common.PageResponse;
import com.linhvecac.user.dto.MemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Danh sách thành viên theo hạng cho admin — chặn ADMIN qua SecurityConfig (/api/admin/**). */
@RestController
@RequestMapping("/api/admin/members")
@RequiredArgsConstructor
public class AdminMemberController {

    private final UserService userService;

    @GetMapping
    public PageResponse<MemberResponse> list(@RequestParam(name = "tier", required = false) Tier tier,
                                             @RequestParam(name = "page", defaultValue = "0") int page,
                                             @RequestParam(name = "size", defaultValue = "20") int size) {
        return userService.adminMembers(tier, page, size);
    }
}
