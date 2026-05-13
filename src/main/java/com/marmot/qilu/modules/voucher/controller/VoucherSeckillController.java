package com.marmot.qilu.modules.voucher.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.modules.voucher.service.VoucherSeckillService;
import com.marmot.qilu.modules.voucher.vo.VoucherSeckillResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
