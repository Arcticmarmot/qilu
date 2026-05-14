package com.marmot.qilu.modules.voucher.service;

import com.marmot.qilu.modules.voucher.vo.VoucherOrderVO;

import java.util.List;

public interface VoucherOrderService {

    void createVoucherOrder(String userUuid, Long voucherId, Long seckillId);

    List<VoucherOrderVO> getMyVoucherOrders();

    VoucherOrderVO getVoucherOrderDetail(String orderNo);
}
