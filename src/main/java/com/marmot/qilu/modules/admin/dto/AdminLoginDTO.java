package com.marmot.qilu.modules.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "管理员请求登录")
public class AdminLoginDTO {

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "密码", example = "123456")
    private String password;
}
