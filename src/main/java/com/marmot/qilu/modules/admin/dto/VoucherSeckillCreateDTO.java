package com.marmot.qilu.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "创建兑换券秒杀活动请求")
public class VoucherSeckillCreateDTO {

    @NotNull(message = "voucher id is required")
    @Schema(description = "兑换券ID", example = "1")
    private Long voucherId;

    @NotNull(message = "total stock is required")
    @Min(value = 1, message = "total stock must be positive")
    @Schema(description = "活动总库存", example = "100")
    private Integer totalStock;

    @NotNull(message = "start time is required")
    @Schema(description = "秒杀开始时间", example = "2026-05-13T20:00:00")
    private LocalDateTime startTime;

    @NotNull(message = "end time is required")
    @Schema(description = "秒杀结束时间", example = "2026-05-13T20:10:00")
    private LocalDateTime endTime;

    @NotNull(message = "redeem deadline is required")
    @Schema(description = "兑换截止时间", example = "2026-05-20T23:59:59")
    private LocalDateTime redeemDeadline;
}
