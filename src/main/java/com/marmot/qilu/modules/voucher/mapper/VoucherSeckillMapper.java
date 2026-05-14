package com.marmot.qilu.modules.voucher.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.marmot.qilu.modules.voucher.entity.VoucherSeckill;
import com.marmot.qilu.modules.voucher.vo.VoucherSeckillVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface VoucherSeckillMapper extends BaseMapper<VoucherSeckill> {

    int decreaseRemainingStock(@Param("seckillId") Long seckillId);

    List<VoucherSeckillVO> selectAvailableVoucherSeckills(@Param("now") LocalDateTime now);

    VoucherSeckillVO selectVoucherSeckillDetail(@Param("seckillId") Long seckillId);
}
