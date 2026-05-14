package com.marmot.qilu.modules.voucher.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.marmot.qilu.modules.voucher.entity.VoucherOrder;
import com.marmot.qilu.modules.voucher.vo.VoucherOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface VoucherOrderMapper extends BaseMapper<VoucherOrder> {

    int redeemByRedeemCode(@Param("redeemCode") String redeemCode, @Param("now") LocalDateTime now);

    List<VoucherOrderVO> selectMyVoucherOrders(@Param("currUserUuid") String currUserUuid);

    VoucherOrderVO selectVoucherOrderDetail(@Param("currUserUuid") String currUserUuid, @Param("orderNo") String orderNo);
}
