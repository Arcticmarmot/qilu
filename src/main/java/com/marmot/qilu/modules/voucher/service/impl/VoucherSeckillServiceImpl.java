package com.marmot.qilu.modules.voucher.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.event.voucher.VoucherOrderEvent;
import com.marmot.qilu.common.event.voucher.VoucherOrderProducer;
import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.common.exception.ConflictException;
import com.marmot.qilu.common.exception.NotFoundException;
import com.marmot.qilu.modules.voucher.constant.VoucherRedisKeys;
import com.marmot.qilu.modules.voucher.entity.VoucherSeckill;
import com.marmot.qilu.modules.voucher.mapper.VoucherOrderMapper;
import com.marmot.qilu.modules.voucher.mapper.VoucherSeckillMapper;
import com.marmot.qilu.modules.voucher.service.VoucherSeckillService;
import com.marmot.qilu.modules.voucher.vo.VoucherSeckillResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherSeckillServiceImpl implements VoucherSeckillService {

    private static final int STATUS_NORMAL = 1;

    private static final long LUA_SUCCESS = 0L;
    private static final long LUA_STOCK_NOT_ENOUGH = 1L;
    private static final long LUA_DUPLICATE_SECKILL = 2L;
    private static final long LUA_STOCK_NOT_INITIALIZED = 3L;

    private final VoucherSeckillMapper voucherSeckillMapper;
    private final VoucherOrderProducer voucherOrderProducer;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public VoucherSeckillResultVO seckillVoucher(Long seckillId) {
        validateSeckillId(seckillId);

        String currUserUuid = UserContext.requireUuid();

        VoucherSeckill voucherSeckill = voucherSeckillMapper.selectOne(
                Wrappers.<VoucherSeckill>lambdaQuery()
                        .eq(VoucherSeckill::getId, seckillId)
                        .eq(VoucherSeckill::getStatus, STATUS_NORMAL)
        );
        if (voucherSeckill == null) {
            throw new NotFoundException("voucher seckill not found or disabled");
        }

        validateSeckillTime(voucherSeckill.getStartTime(), voucherSeckill.getEndTime());

        Long result = executeVoucherSeckillScript(seckillId, currUserUuid);
        handleScriptResult(result);

        VoucherOrderEvent event = new VoucherOrderEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setSeckillId(seckillId);
        event.setUserUuid(currUserUuid);
        event.setVoucherId(voucherSeckill.getVoucherId());
        event.setOccurredAt(LocalDateTime.now());

        voucherOrderProducer.sendVoucherOrderEvent(event);

        log.info("voucher seckill accepted, userUuid={}, seckillId={}, voucherId={}",
                currUserUuid, seckillId, voucherSeckill.getVoucherId());
        return new VoucherSeckillResultVO("processing");
    }

    private Long executeVoucherSeckillScript(Long seckillId, String userUuid) {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setLocation(new ClassPathResource("lua/voucher_seckill.lua"));
        redisScript.setResultType(Long.class);

        String stockKey = VoucherRedisKeys.seckillStockKey(seckillId);
        String userKey = VoucherRedisKeys.seckillUserKey(seckillId);

        return stringRedisTemplate.execute(
                redisScript,
                List.of(stockKey, userKey),
                userUuid
        );
    }

    private void handleScriptResult(Long result) {
        if(result == null) {
            throw new IllegalStateException("execute voucher seckill script failed");
        }

        if(result == LUA_SUCCESS) {
            return;
        }

        if(result == LUA_STOCK_NOT_ENOUGH) {
            throw new BadRequestException("voucher stock is not enough");
        }

        if(result == LUA_DUPLICATE_SECKILL) {
            throw new ConflictException("duplicate voucher seckill");
        }

        if(result == LUA_STOCK_NOT_INITIALIZED) {
            throw new BadRequestException("voucher seckill stock is not initialized");
        }

        throw new IllegalStateException("unknown voucher seckill result");
    }

    private void validateSeckillId(Long seckillId) {
        if (seckillId == null || seckillId <= 0) {
            throw new BadRequestException("seckill id is invalid");
        }
    }

    private void validateSeckillTime(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();

        if(startTime == null || endTime == null) {
            throw new IllegalStateException("voucher seckill time is invalid");
        }
        if(now.isBefore(startTime)) {
            throw new BadRequestException("voucher seckill has not started");
        }
        if(now.isAfter(endTime)) {
            throw new BadRequestException("voucher seckill has ended");
        }
    }

}
