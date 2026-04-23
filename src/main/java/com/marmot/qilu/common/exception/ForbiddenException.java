package com.marmot.qilu.common.exception;

import com.marmot.qilu.common.api.ErrorCode;

public class ForbiddenException extends BizException {

    public ForbiddenException(String message) {
        super(ErrorCode.FORBIDDEN, message);
    }
}
