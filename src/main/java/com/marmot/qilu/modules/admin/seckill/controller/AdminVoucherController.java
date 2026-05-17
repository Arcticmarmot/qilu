package com.marmot.qilu.modules.admin.seckill.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.modules.admin.seckill.dto.VoucherCreateDTO;
import com.marmot.qilu.modules.admin.seckill.dto.VoucherRedeemDTO;
import com.marmot.qilu.modules.admin.seckill.dto.VoucherSeckillCreateDTO;
import com.marmot.qilu.modules.admin.seckill.service.AdminVoucherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AdminVoucher", description = "优惠券管理模块")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminVoucherController {

    private final AdminVoucherService adminVoucherService;

    @Operation(summary = "创建优惠券")
    @PostMapping("/vouchers")
    public ApiResponse<Long> createVoucher(@Valid @RequestBody VoucherCreateDTO dto) {
        return ApiResponse.success(adminVoucherService.createVoucher(dto));
    }

    @Operation(summary = "创建秒杀活动")
    @PostMapping("/voucher-seckills")
    public ApiResponse<Long> createVoucherSeckill(@Valid @RequestBody VoucherSeckillCreateDTO dto) {
        return ApiResponse.success(adminVoucherService.createVoucherSeckill(dto));
    }

    @Operation(summary = "预热秒杀活动库存")
    @PostMapping("/voucher-seckills/{seckillId}/preheat")
    public ApiResponse<Void> preheatVoucherSeckill(@PathVariable Long seckillId) {
        adminVoucherService.preheatVoucherSeckill(seckillId);
        return ApiResponse.success();
    }

    @Operation(summary = "核销兑换码")
    @PatchMapping("/voucher-orders/redeem")
    public ApiResponse<Void> redeemVoucherOrder(@Valid @RequestBody VoucherRedeemDTO dto) {
        adminVoucherService.redeemVoucherOrder(dto);
        return ApiResponse.success();
    }
}
