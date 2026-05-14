package com.marmot.qilu.modules.voucher.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.modules.voucher.service.VoucherSeckillService;
import com.marmot.qilu.modules.voucher.vo.VoucherSeckillOrderResultVO;
import com.marmot.qilu.modules.voucher.vo.VoucherSeckillResultVO;
import com.marmot.qilu.modules.voucher.vo.VoucherSeckillVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "VoucherSeckill", description = "优惠券秒杀接口")
@RestController
@RequestMapping("/voucher-seckills")
@RequiredArgsConstructor
public class VoucherSeckillController {

    private final VoucherSeckillService voucherSeckillService;

    @Operation(summary = "秒杀优惠券")
    @PostMapping("/{seckillId}/orders")
    public ApiResponse<VoucherSeckillResultVO> seckillVoucher(@PathVariable Long seckillId) {
        return ApiResponse.success(voucherSeckillService.seckillVoucher(seckillId));
    }

    @Operation(summary = "查询抢券结果")
    @GetMapping("/{seckillId}/orders/result")
    public ApiResponse<VoucherSeckillOrderResultVO> getSeckillOrderResult(@PathVariable Long seckillId) {
        return ApiResponse.success(voucherSeckillService.getSeckillOrderResult(seckillId));
    }

    @Operation(summary = "查询可用秒杀活动列表")
    @GetMapping
    public ApiResponse<List<VoucherSeckillVO>> getAvailableVoucherSeckills() {
        return ApiResponse.success(voucherSeckillService.getAvailableVoucherSeckills());
    }

    @Operation(summary = "查询秒杀活动详情")
    @GetMapping("/{seckillId}")
    public ApiResponse<VoucherSeckillVO> getVoucherSeckillDetail(@PathVariable Long seckillId) {
        return ApiResponse.success(voucherSeckillService.getVoucherSeckillDetail(seckillId));
    }
}
