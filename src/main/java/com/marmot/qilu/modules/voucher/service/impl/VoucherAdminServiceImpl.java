package com.marmot.qilu.modules.voucher.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.common.exception.NotFoundException;
import com.marmot.qilu.modules.voucher.constant.VoucherRedisKeys;
import com.marmot.qilu.modules.voucher.dto.VoucherCreateDTO;
import com.marmot.qilu.modules.voucher.dto.VoucherRedeemDTO;
import com.marmot.qilu.modules.voucher.dto.VoucherSeckillCreateDTO;
import com.marmot.qilu.modules.voucher.entity.Voucher;
import com.marmot.qilu.modules.voucher.entity.VoucherOrder;
import com.marmot.qilu.modules.voucher.entity.VoucherSeckill;
import com.marmot.qilu.modules.voucher.enums.VoucherOrderStatus;
import com.marmot.qilu.modules.voucher.mapper.VoucherMapper;
import com.marmot.qilu.modules.voucher.mapper.VoucherOrderMapper;
import com.marmot.qilu.modules.voucher.mapper.VoucherSeckillMapper;
import com.marmot.qilu.modules.voucher.service.VoucherAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.marmot.qilu.common.util.ContentUtils.normalizeContent;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherAdminServiceImpl implements VoucherAdminService {

    private static final int STATUS_DELETED = 0;
    private static final int STATUS_NORMAL = 1;
    private static final int MAX_VOUCHER_TITLE_LENGTH = 128;
    private static final int MAX_VOUCHER_DESCRIPTION_LENGTH = 512;
    private static final int MAX_REDEEM_CODE_LENGTH = 64;

    private final VoucherMapper voucherMapper;
    private final VoucherSeckillMapper voucherSeckillMapper;
    private final VoucherOrderMapper voucherOrderMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createVoucher(VoucherCreateDTO dto) {
        if(dto == null) {
            throw new BadRequestException("voucher create dto is invalid");
        }
        String title = dto.getTitle();
        String description = normalizeContent(dto.getDescription());
        validateVoucherTitle(title);
        validateVoucherDescription(description);

        Voucher voucher = new Voucher();
        voucher.setTitle(title);
        voucher.setDescription(description);
        voucher.setStatus(STATUS_NORMAL);

        int inserted = voucherMapper.insert(voucher);
        if(inserted != 1) {
            throw new IllegalStateException("create voucher failed");
        }
        log.info("create voucher success, voucherId={}", voucher.getId());

        return voucher.getId();
    }

    @Override
    public Long createVoucherSeckill(VoucherSeckillCreateDTO dto) {
        if(dto == null) {
            throw new BadRequestException("voucher seckill create dto is invalid");
        }
        Long voucherId = dto.getVoucherId();
        if(voucherId == null) {
            throw new BadRequestException("voucher id is invalid");
        }
        boolean exists = voucherMapper.exists(
                Wrappers.<Voucher>lambdaQuery()
                        .eq(Voucher::getId, dto.getVoucherId())
                        .eq(Voucher::getStatus, STATUS_NORMAL)
        );
        if (!exists) {
            throw new NotFoundException("voucher not found or disabled");
        }
        VoucherSeckill voucherSeckill = new VoucherSeckill();
        voucherSeckill.setVoucherId(dto.getVoucherId());
        voucherSeckill.setTotalStock(dto.getTotalStock());
        voucherSeckill.setRemainingStock(dto.getTotalStock());
        voucherSeckill.setStartTime(dto.getStartTime());
        voucherSeckill.setEndTime(dto.getEndTime());
        voucherSeckill.setRedeemDeadline(dto.getRedeemDeadline());
        voucherSeckill.setStatus(STATUS_NORMAL);

        int inserted = voucherSeckillMapper.insert(voucherSeckill);
        if (inserted != 1) {
            throw new IllegalStateException("create voucher seckill failed");
        }

        log.info("create voucher seckill success, seckillId={}, voucherId={}",
                voucherSeckill.getId(), dto.getVoucherId());

        return voucherSeckill.getId();
    }

    @Override
    public void preheatVoucherSeckill(Long seckillId) {
        validateSeckillId(seckillId);

        VoucherSeckill voucherSeckill = voucherSeckillMapper.selectOne(
                Wrappers.<VoucherSeckill>lambdaQuery()
                        .eq(VoucherSeckill::getId, seckillId)
                        .eq(VoucherSeckill::getStatus, STATUS_NORMAL)
        );
        if(voucherSeckill == null) {
            throw new NotFoundException("voucher seckill not found or disabled");
        }
        LocalDateTime now = LocalDateTime.now();
        if(!voucherSeckill.getEndTime().isAfter(now)) {
            throw new BadRequestException("voucher seckill has ended");
        }

        String stockKey = VoucherRedisKeys.seckillStockKey(seckillId);
        String userKey = VoucherRedisKeys.seckillUserKey(seckillId);

        stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(voucherSeckill.getRemainingStock()));
        stringRedisTemplate.delete(userKey);

        log.info("preheat voucher seckill success, seckillId={}, remainingStock={}",
                seckillId, voucherSeckill.getRemainingStock());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void redeemVoucherOrder(VoucherRedeemDTO dto) {
        if(dto == null) {
            throw new BadRequestException("voucher redeem dto is invalid");
        }
        String redeemCode = dto.getRedeemCode();
        validateRedeemCode(redeemCode);

        VoucherOrder voucherOrder = voucherOrderMapper.selectOne(
                Wrappers.<VoucherOrder>lambdaQuery()
                        .eq(VoucherOrder::getRedeemCode, redeemCode)
        );

        if (voucherOrder == null) {
            throw new NotFoundException("redeem code is invalid");
        }

        if(!VoucherOrderStatus.UNUSED.getCode().equals(voucherOrder.getStatus())) {
            throw new BadRequestException("voucher order is not unused");
        }

        LocalDateTime now = LocalDateTime.now();
        if(!voucherOrder.getExpireAt().isAfter(now)) {
            throw new BadRequestException("voucher order has expired");
        }

        int updated = voucherOrderMapper.redeemByRedeemCode(redeemCode, now);
        if(updated != 1) {
            throw new IllegalStateException("redeem voucher order failed");
        }
        log.info("redeem voucher order success, orderNo={}, userUuid={}, seckillId={}",
                voucherOrder.getOrderNo(), voucherOrder.getUserUuid(), voucherOrder.getSeckillId());
    }

    private void validateRedeemCode(String redeemCode) {
        if(redeemCode == null || redeemCode.trim().isBlank()) {
            throw new BadRequestException("redeem code is required");
        }

        if(redeemCode.length() > MAX_REDEEM_CODE_LENGTH) {
            throw new BadRequestException("redeem code is too long");
        }
    }

    private void validateSeckillId(Long seckillId) {
        if (seckillId == null || seckillId <= 0) {
            throw new BadRequestException("seckill id is invalid");
        }
    }

    private void validateVoucherTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new BadRequestException("voucher title is required");
        }

        if (title.length() > MAX_VOUCHER_TITLE_LENGTH) {
            throw new BadRequestException("voucher title is too long");
        }
    }

    private void validateVoucherDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new BadRequestException("voucher title is required");
        }

        if (description.length() > MAX_VOUCHER_DESCRIPTION_LENGTH) {
            throw new BadRequestException("voucher title is too long");
        }
    }

}
