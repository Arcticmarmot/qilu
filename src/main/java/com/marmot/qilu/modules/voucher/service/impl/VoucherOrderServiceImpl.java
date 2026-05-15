package com.marmot.qilu.modules.voucher.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.common.exception.NotFoundException;
import com.marmot.qilu.common.util.VoucherUtils;
import com.marmot.qilu.modules.voucher.entity.VoucherOrder;
import com.marmot.qilu.modules.voucher.entity.VoucherSeckill;
import com.marmot.qilu.modules.voucher.enums.VoucherOrderStatus;
import com.marmot.qilu.modules.voucher.mapper.VoucherOrderMapper;
import com.marmot.qilu.modules.voucher.mapper.VoucherSeckillMapper;
import com.marmot.qilu.modules.voucher.service.VoucherOrderService;
import com.marmot.qilu.modules.voucher.vo.VoucherOrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherOrderServiceImpl implements VoucherOrderService {

    private static final int STATUS_NORMAL = 1;

    private final VoucherOrderMapper voucherOrderMapper;
    private final VoucherSeckillMapper voucherSeckillMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createVoucherOrder(String userUuid, Long voucherId, Long seckillId) {
        validateUserUuid(userUuid);
        validateVoucherId(voucherId);
        validateSeckillId(seckillId);

        VoucherSeckill voucherSeckill = voucherSeckillMapper.selectOne(
                Wrappers.<VoucherSeckill>lambdaQuery()
                        .eq(VoucherSeckill::getId, seckillId)
                        .eq(VoucherSeckill::getVoucherId, voucherId)
                        .eq(VoucherSeckill::getStatus, STATUS_NORMAL)
        );
        if (voucherSeckill == null) {
            throw new IllegalStateException("voucher seckill not found when creating order");
        }

        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setOrderNo(VoucherUtils.generateOrderNo());
        voucherOrder.setVoucherId(voucherId);
        voucherOrder.setSeckillId(seckillId);
        voucherOrder.setUserUuid(userUuid);
        voucherOrder.setStatus(VoucherOrderStatus.UNUSED.getCode());
        voucherOrder.setRedeemCode(VoucherUtils.generateRedeemCode());
        voucherOrder.setExpireAt(voucherSeckill.getEndTime().plusDays(1));

        try {
            int inserted = voucherOrderMapper.insert(voucherOrder);
            if(inserted != 1) {
                throw new IllegalStateException("create voucher order failed");
            }
        } catch (DuplicateKeyException e) {
            log.warn("duplicate voucher order ignored, seckillId={}, voucherId={}, userUuid={}",
                    seckillId, voucherId, userUuid);
            throw new DuplicateKeyException("duplicated voucher order");
        }

        int updated = voucherSeckillMapper.decreaseRemainingStock(seckillId);
        if(updated != 1) {
            throw new IllegalStateException("decrease voucher seckill stock failed");
        }

        log.info("create voucher order success, orderNo={}, userUuid={}, seckillId={}, voucherId={}",
                voucherOrder.getOrderNo(), userUuid, seckillId, voucherId);

        return voucherOrder.getId();
    }

    @Override
    public List<VoucherOrderVO> getMyVoucherOrders() {
        String userUuid = UserContext.requireUuid();
        return voucherOrderMapper.selectMyVoucherOrders(userUuid);
    }

    @Override
    public VoucherOrderVO getVoucherOrderDetail(String orderNo) {
        validateOrderNo(orderNo);

        String userUuid = UserContext.requireUuid();
        VoucherOrderVO vo = voucherOrderMapper.selectVoucherOrderDetail(userUuid, orderNo);
        if(vo == null) {
            throw new NotFoundException("voucher order not found or no permission");
        }
        return vo;
    }

    private void validateOrderNo(String orderNo) {
        if (orderNo == null || orderNo.isBlank()) {
            throw new BadRequestException("order no is required");
        }

        if (orderNo.length() > 64) {
            throw new BadRequestException("order no is too long");
        }
    }

    private void validateSeckillId(Long seckillId) {
        if (seckillId == null || seckillId <= 0) {
            throw new BadRequestException("seckill id is invalid");
        }
    }

    private void validateVoucherId(Long voucherId) {
        if(voucherId == null || voucherId <= 0) {
            throw new BadRequestException("voucher id is invalid");
        }
    }

    private void validateUserUuid(String userUuid) {
        if(userUuid == null || userUuid.isBlank()) {
            throw new BadRequestException("user uuid is invalid");
        }
    }
}
