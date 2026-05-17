package com.marmot.qilu.modules.admin.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "用户分页列表")
public class AdminUserPageVO<T> {

    @Schema(description = "当前页码", example = "1")
    private long current;

    @Schema(description = "每页条数", example = "10")
    private long size;

    @Schema(description = "总记录数", example = "25")
    private long total;

    @Schema(description = "当前页数据")
    private List<T> records;
}
