package com.linhvecac.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Lỗi nghiệp vụ có chủ đích — message tiếng Việt hiển thị thẳng cho người dùng. */
@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
