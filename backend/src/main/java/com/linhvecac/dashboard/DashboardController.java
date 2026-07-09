package com.linhvecac.dashboard;

import com.linhvecac.dashboard.dto.DashboardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Số liệu tổng quan cho admin — chặn ADMIN qua SecurityConfig (/api/admin/**). */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public DashboardResponse get(@RequestParam(defaultValue = "30") int days) {
        return dashboardService.getDashboard(days);
    }
}
