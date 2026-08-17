package com.example.gym_server.exception;

import com.example.gym_server.constant.ErrorCode;
import com.example.gym_server.dto.ApiResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

/**
 * 全局异常处理，统一返回 ApiResponse 格式。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusiness(BusinessException e) {
        HttpStatus status = switch (e.getCode()) {
            case ErrorCode.PASSWORD_WRONG, ErrorCode.TOKEN_INVALID -> HttpStatus.UNAUTHORIZED;
            case ErrorCode.ACCESS_DENIED -> HttpStatus.FORBIDDEN;
            case ErrorCode.RESOURCE_NOT_FOUND, ErrorCode.REFUND_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case ErrorCode.COURSE_FULL, ErrorCode.ALREADY_BOOKED, ErrorCode.ALREADY_INSIDE,
                 ErrorCode.APPLY_ALREADY_SUBMITTED, ErrorCode.BUSINESS_CONFLICT -> HttpStatus.CONFLICT;
            default -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(ApiResponse.error(e.getCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .orElse("参数校验失败");
        return ResponseEntity.badRequest().body(ApiResponse.error(ErrorCode.PARAM_INVALID, msg));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleDbConstraint(DataIntegrityViolationException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ErrorCode.DB_ERROR, "数据冲突，可能已存在相同记录"));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(ErrorCode.RESOURCE_NOT_FOUND, "资源不存在"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ErrorCode.BUSINESS_CONFLICT, e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<?>> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(ErrorCode.BUSINESS_CONFLICT, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneric(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.SYSTEM_BUSY, "系统繁忙：" + e.getMessage()));
    }
}
