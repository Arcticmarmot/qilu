package com.marmot.qilu.common.exception;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.common.api.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleRuntimeException(BizException e) {
        log.warn("business exception occurred, code={}, message={}", e.getCode(), e.getMessage());
        return ApiResponse.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception e) {
        log.error("unhandled system exception occurred", e);
        return ApiResponse.fail(ErrorCode.SYSTEM_ERROR, "server internal error");
    }
}
