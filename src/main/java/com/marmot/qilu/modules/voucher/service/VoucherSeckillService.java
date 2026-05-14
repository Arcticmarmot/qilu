package com.marmot.qilu.modules.voucher.service;

import com.marmot.qilu.modules.voucher.vo.VoucherSeckillOrderResultVO;
import com.marmot.qilu.modules.voucher.vo.VoucherSeckillResultVO;
import com.marmot.qilu.modules.voucher.vo.VoucherSeckillVO;

import java.util.List;

public interface VoucherSeckillService {

    VoucherSeckillResultVO seckillVoucher(Long seckillId);

    VoucherSeckillOrderResultVO getSeckillOrderResult(Long seckillId);

    List<VoucherSeckillVO> getAvailableVoucherSeckills();

    VoucherSeckillVO getVoucherSeckillDetail(Long seckillId);
}
