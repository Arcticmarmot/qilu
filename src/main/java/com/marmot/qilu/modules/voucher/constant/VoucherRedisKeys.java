package com.marmot.qilu.modules.voucher.constant;

public final class VoucherRedisKeys {

    private VoucherRedisKeys() {}

    public static String seckillStockKey(Long seckillId) {
        return "voucher::seckill::stock:" + seckillId;
    }

    public static String seckillUserKey(Long seckillId) {
        return "voucher::seckill::users:" + seckillId;
    }
}
