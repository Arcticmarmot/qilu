package com.marmot.qilu.modules.admin.service;

import com.marmot.qilu.modules.admin.dto.VoucherCreateDTO;
import com.marmot.qilu.modules.admin.dto.VoucherRedeemDTO;
import com.marmot.qilu.modules.admin.dto.VoucherSeckillCreateDTO;

public interface AdminVoucherService {

    Long createVoucher(VoucherCreateDTO dto);

    Long createVoucherSeckill(VoucherSeckillCreateDTO dto);

    void preheatVoucherSeckill(Long seckillId);

    void redeemVoucherOrder(VoucherRedeemDTO dto);
}
