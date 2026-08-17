package com.example.gym_server.exception;

/**
 * 业务异常，携带业务错误码（见 ErrorCode）。
 */
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public static BusinessException of(int code, String message) {
        return new BusinessException(code, message);
    }

    public int getCode() {
        return code;
    }
}
