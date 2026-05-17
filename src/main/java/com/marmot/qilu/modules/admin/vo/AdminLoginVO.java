package com.marmot.qilu.modules.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "管理员登录响应")
public class AdminLoginVO {

    @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "当前登录管理员UUID", example = "5ee308293252481fb489b21a8d0f8d8d")
    private String uuid;

    @Schema(description = "当前登录管理员名", example = "admin")
    private String username;

    @Schema(description = "当前登录管理员角色", example = "root")
    private String role;
}
