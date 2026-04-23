package com.marmot.qilu.common.exception;

import com.marmot.qilu.common.api.ErrorCode;

public class NotFoundException extends BizException {

    public NotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, message);
    }
}
