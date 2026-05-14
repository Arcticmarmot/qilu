package com.marmot.qilu.modules.voucher.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "优惠券订单信息")
public class VoucherOrderVO {

    @Schema(description = "订单号", example = "QV202605132230001234")
    private String orderNo;

    @Schema(description = "兑换券ID", example = "1")
    private Long voucherId;

    @Schema(description = "秒杀活动ID", example = "1")
    private Long seckillId;

    @Schema(description = "兑换券标题", example = "大龙山登山杖兑换券")
    private String title;

    @Schema(description = "兑换券描述", example = "凭兑换码可到线下门店兑换一次登山杖优惠资格")
    private String description;

    @Schema(description = "兑换码", example = "QILU-VR-ABC123DEF456")
    private String redeemCode;

    @Schema(description = "状态：1待使用 2已使用 3已过期 4已取消", example = "1")
    private Integer status;

    @Schema(description = "过期时间")
    private LocalDateTime expireAt;

    @Schema(description = "核销时间")
    private LocalDateTime usedAt;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
