package com.marmot.qilu.modules.voucher.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.modules.voucher.dto.VoucherCreateDTO;
import com.marmot.qilu.modules.voucher.dto.VoucherRedeemDTO;
import com.marmot.qilu.modules.voucher.dto.VoucherSeckillCreateDTO;
import com.marmot.qilu.modules.voucher.service.VoucherAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "VoucherAdmin", description = "优惠券管理接口")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class VoucherAdminController {

    private final VoucherAdminService voucherAdminService;

    @Operation(summary = "创建优惠券")
    @PostMapping("/vouchers")
    public ApiResponse<Long> createVoucher(@Valid @RequestBody VoucherCreateDTO dto) {
        return ApiResponse.success(voucherAdminService.createVoucher(dto));
    }

    @Operation(summary = "创建秒杀活动")
    @PostMapping("/voucher-seckills")
    public ApiResponse<Long> createVoucherSeckill(@Valid @RequestBody VoucherSeckillCreateDTO dto) {
        return ApiResponse.success(voucherAdminService.createVoucherSeckill(dto));
    }

    @Operation(summary = "预热秒杀活动库存")
    @PostMapping("/voucher-seckills/{seckillId}/preheat")
    public ApiResponse<Void> preheatVoucherSeckill(@PathVariable Long seckillId) {
        voucherAdminService.preheatVoucherSeckill(seckillId);
        return ApiResponse.success();
    }

    @Operation(summary = "核销兑换码")
    @PatchMapping("/voucher-orders/redeem")
    public ApiResponse<Void> redeemVoucherOrder(@Valid @RequestBody VoucherRedeemDTO dto) {
        voucherAdminService.redeemVoucherOrder(dto);
        return ApiResponse.success();
    }
}
