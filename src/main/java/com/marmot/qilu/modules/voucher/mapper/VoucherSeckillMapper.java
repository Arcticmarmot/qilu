package com.marmot.qilu.modules.voucher.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.marmot.qilu.modules.voucher.entity.VoucherSeckill;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VoucherSeckillMapper extends BaseMapper<VoucherSeckill> {

    int decreaseRemainingStock(Long seckillId);
}
