package com.linhvecac.common;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Endpoint kiểm chứng end-to-end của P1: xác nhận backend chạy và kết nối được DB.
 * Trả JSON có payload tiếng Việt cho frontend hiển thị (không dùng Actuator).
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    private static final ZoneId ZONE_VN = ZoneId.of("Asia/Ho_Chi_Minh");

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("time", OffsetDateTime.now(ZONE_VN).toString());

        try {
            String appName = jdbcTemplate.queryForObject(
                    "SELECT meta_value FROM app_meta WHERE meta_key = 'app_name'",
                    String.class);
            body.put("db", "UP");
            body.put("app", appName);
        } catch (Exception e) {
            body.put("db", "DOWN");
            body.put("app", null);
            body.put("dbError", e.getMessage());
        }
        return body;
    }
}
