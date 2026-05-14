package com.marmot.qilu.modules.voucher.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "优惠券秒杀活动信息")
public class VoucherSeckillVO {

    @Schema(description = "秒杀活动ID", example = "1")
    private Long seckillId;

    @Schema(description = "优惠券ID", example = "1")
    private Long voucherId;

    @Schema(description = "优惠券标题", example = "登山杖兑换券")
    private String title;

    @Schema(description = "优惠券描述", example = "凭兑换码可到线下门店兑换一次登山杖优惠资格")
    private String description;

    @Schema(description = "活动总库存", example = "100")
    private Integer totalStock;

    @Schema(description = "数据库剩余库存", example = "88")
    private Integer remainingStock;

    @Schema(description = "秒杀开始时间")
    private LocalDateTime startTime;

    @Schema(description = "秒杀结束时间")
    private LocalDateTime endTime;

    @Schema(description = "活动状态：0禁用 1启用", example = "1")
    private Integer status;
}