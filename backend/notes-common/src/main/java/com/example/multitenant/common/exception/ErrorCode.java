package com.example.multitenant.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // 400
    INVALID_REQUEST(400, "요청 파라미터 오류"),

    // 401
    UNAUTHORIZED(401, "인증 필요"),

    // 403
    FORBIDDEN(403, "권한 없음"),
    PLAN_LIMIT_EXCEEDED(403, "플랜 제한 초과"),

    // 404
    NOT_FOUND(404, "리소스 없음"),
    TENANT_NOT_FOUND(404, "테넌트 없음"),

    // 409
    DUPLICATE(409, "중복 데이터"),

    // 429
    RATE_LIMIT_EXCEEDED(429, "요청 제한 초과");

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
