package com.marmot.qilu.modules.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "注册用户请求")
public class UserCreateDTO {

    @Schema(description = "昵称", example = "marmot")
    private String nickname;

    @Schema(description = "邮箱", example = "marmot@example.com")
    private String email;

    @Schema(description = "密码", example = "123456")
    private String password;
}
