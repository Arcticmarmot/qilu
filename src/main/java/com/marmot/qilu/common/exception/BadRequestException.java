package com.marmot.qilu.common.exception;

import com.marmot.qilu.common.api.ErrorCode;

public class BadRequestException extends BizException {

    public BadRequestException(String message) {
        super(ErrorCode.BAD_REQUEST, message);
    }
}
