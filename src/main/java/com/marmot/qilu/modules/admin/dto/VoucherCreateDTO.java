package com.marmot.qilu.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "创建兑换券请求")
public class VoucherCreateDTO {

    @NotBlank(message = "voucher title is required")
    @Size(max = 128, message = "voucher title is too long")
    @Schema(description = "兑换券标题", example = "登山杖兑换券")
    private String title;

    @Size(max = 512, message = "voucher description is too long")
    @Schema(description = "兑换券描述", example = "凭兑换码可到线下门店兑换一次登山杖优惠资格")
    private String description;
}
