package com.marmot.qilu.modules.voucher.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VoucherOrderStatus {

    UNUSED(1, "unused"),
    USED(2, "used"),
    EXPIRED(3, "expired"),
    CANCELLED(4, "cancelled");

    private final Integer code;
    private final String value;
}
