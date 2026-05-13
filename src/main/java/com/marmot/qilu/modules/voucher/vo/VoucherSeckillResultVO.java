package com.marmot.qilu.modules.voucher.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "秒杀请求结果")
public class VoucherSeckillResultVO {

    @Schema(description = "结果状态", example = "processing")
    private String status;
}
