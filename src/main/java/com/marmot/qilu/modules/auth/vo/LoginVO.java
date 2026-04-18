package com.marmot.qilu.modules.auth.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "登录响应")
public class LoginVO {

    @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "当前登录用户UUID", example = "5ee308293252481fb489b21a8d0f8d8d")
    private String uuid;

    @Schema(description = "当前登录用户昵称", example = "marmot")
    private String nickname;

    @Schema(description = "当前登录用户邮箱", example = "marmot@example.com")
    private String email;
}
