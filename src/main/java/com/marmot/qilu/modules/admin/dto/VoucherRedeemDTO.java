package com.marmot.qilu.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "核销兑换码请求")
public class VoucherRedeemDTO {

    @NotBlank(message = "redeem code is required")
    @Size(max = 64, message = "redeem code is too long")
    @Schema(description = "兑换码", example = "QILU-VR-9F3A8K2P")
    private String redeemCode;
}