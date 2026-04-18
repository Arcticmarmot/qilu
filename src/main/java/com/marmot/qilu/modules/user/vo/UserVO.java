package com.marmot.qilu.modules.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "当前登录用户信息")
public class UserVO {

    @Schema(description = "用户UUID", example = "5ee308293252481fb489b21a8d0f8d8d")
    private String uuid;

    @Schema(description = "昵称", example = "marmot")
    private String nickname;

    @Schema(description = "邮箱", example = "marmot@example.com")
    private String email;

    @Schema(description = "账号状态 1-正常 0-封禁", example = "1")
    private Integer status;

    @Schema(description = "用户注册时间", example = "2026-04-16 01:59:26")
    private LocalDateTime createAt;
}