package com.marmot.qilu.common.exception;

import com.marmot.qilu.common.api.ErrorCode;

public class ConflictException extends BizException {

    public ConflictException(String message) {
        super(ErrorCode.CONFLICT, message);
    }
}
