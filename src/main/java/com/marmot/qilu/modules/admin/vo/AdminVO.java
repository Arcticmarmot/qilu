package com.marmot.qilu.modules.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "管理员基本信息")
public class AdminVO {

    @Schema(description = "当前登录管理员UUID", example = "5ee308293252481fb489b21a8d0f8d8d")
    private String uuid;

    @Schema(description = "当前登录管理员名", example = "admin")
    private String username;

    @Schema(description = "当前登录管理员角色", example = "root")
    private String role;

    @Schema(description = "账号状态 1-正常 0-封禁", example = "1")
    private Integer status;

    @Schema(description = "管理员注册时间", example = "2026-04-16 01:59:26")
    private LocalDateTime createAt;
}
