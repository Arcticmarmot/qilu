package com.marmot.qilu.modules.admin.seckill.service;

import com.marmot.qilu.modules.admin.seckill.dto.VoucherCreateDTO;
import com.marmot.qilu.modules.admin.seckill.dto.VoucherRedeemDTO;
import com.marmot.qilu.modules.admin.seckill.dto.VoucherSeckillCreateDTO;

public interface AdminVoucherService {

    Long createVoucher(VoucherCreateDTO dto);

    Long createVoucherSeckill(VoucherSeckillCreateDTO dto);

    void preheatVoucherSeckill(Long seckillId);

    void redeemVoucherOrder(VoucherRedeemDTO dto);
}
