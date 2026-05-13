package com.marmot.qilu.modules.voucher.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.marmot.qilu.modules.voucher.entity.VoucherOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface VoucherOrderMapper extends BaseMapper<VoucherOrder> {

    int redeemByRedeemCode(@Param("redeemCode") String redeemCode, @Param("now") LocalDateTime now);
}
