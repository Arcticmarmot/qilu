package com.marmot.qilu.modules.voucher.service;

import com.marmot.qilu.modules.voucher.dto.VoucherCreateDTO;
import com.marmot.qilu.modules.voucher.dto.VoucherRedeemDTO;
import com.marmot.qilu.modules.voucher.dto.VoucherSeckillCreateDTO;

public interface VoucherAdminService {

    void createVoucher(VoucherCreateDTO dto);

    void createVoucherSeckill(VoucherSeckillCreateDTO dto);

    void preheatVoucherSeckill(Long seckillId);

    void redeemVoucherOrder(VoucherRedeemDTO dto);
}
