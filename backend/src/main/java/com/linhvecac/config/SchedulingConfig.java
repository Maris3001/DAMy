package com.linhvecac.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Bật @Scheduled cho các job nền (dọn hold ghế hết hạn, P7: sinh ưu đãi). */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
