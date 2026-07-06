package com.linhvecac.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/** Format lỗi JSON thống nhất toàn API: message chung + lỗi theo field (nếu là lỗi validation). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String message, Map<String, String> errors) {

    public static ErrorResponse of(String message) {
        return new ErrorResponse(message, null);
    }
}
