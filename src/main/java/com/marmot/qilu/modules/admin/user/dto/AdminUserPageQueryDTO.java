package com.marmot.qilu.modules.admin.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "用户分页查询")
public class AdminUserPageQueryDTO {

    @Schema(description = "页码，从 1 开始", example = "1")
    @Min(value = 1, message = "current page must be >= 1")
    private long current = 1;

    @Schema(description = "每页条数", example = "10")
    @Min(value = 1, message = "page size must be >= 1")
    private long size = 10;

    @Schema(description = "用户UUID", example = "5ee308293252481fb489b21a8d0f8d8d")
    private String uuid;

    @Schema(description = "昵称", example = "marmot")
    private String nickname;

    @Schema(description = "邮箱", example = "marmot@example.com")
    private String email;

    @Schema(description = "账号状态 1-正常 0-封禁", example = "1")
    private Integer status;
}
