package com.marmot.qilu.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class VoucherUtils {

    private static final DateTimeFormatter ORDER_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private VoucherUtils() {}

    public static String generateOrderNo() {
        String time = LocalDateTime.now().format(ORDER_TIME_FORMATTER);
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return "QV" + time + suffix;
    }

    public static String generateRedeemCode() {
        String suffix = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return "QILU-VR-" + suffix;
    }
}
