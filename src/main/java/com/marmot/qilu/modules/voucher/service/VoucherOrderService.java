package com.marmot.qilu.modules.voucher.service;

public interface VoucherOrderService {

    void createVoucherOrder(String userUuid, Long voucherId, Long seckillId);
}
