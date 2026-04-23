package com.marmot.qilu.common.exception;

import com.marmot.qilu.common.api.ErrorCode;

public class UnauthorizedException extends BizException {

    public UnauthorizedException(String message) {
        super(ErrorCode.UNAUTHORIZED, message);
    }
}
