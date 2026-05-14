package com.marmot.qilu.modules.voucher.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.modules.voucher.service.VoucherOrderService;
import com.marmot.qilu.modules.voucher.vo.VoucherOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "VoucherOrder", description = "兑换券订单接口")
@RestController
@RequestMapping("/voucher-orders")
@RequiredArgsConstructor
public class VoucherOrderController {

    private final VoucherOrderService voucherOrderService;

    @Operation(summary = "查询我的兑换券订单")
    @GetMapping("/me")
    public ApiResponse<List<VoucherOrderVO>> getMyVoucherOrders() {
        return ApiResponse.success(voucherOrderService.getMyVoucherOrders());
    }

    @Operation(summary = "查询兑换券订单详情")
    @GetMapping("/{orderNo}")
    public ApiResponse<VoucherOrderVO> getVoucherOrderDetail(@PathVariable String orderNo) {
        return ApiResponse.success(voucherOrderService.getVoucherOrderDetail(orderNo));
    }
}