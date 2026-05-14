package com.marmot.qilu.modules.voucher.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "秒杀订单结果")
public class VoucherSeckillOrderResultVO {

    @Schema(description = "订单号", example = "QV202605132230001234")
    private String orderNo;

    @Schema(description = "结果状态：processing/success", example = "success")
    private String status;
}
