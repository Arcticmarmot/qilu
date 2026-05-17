package com.marmot.qilu.modules.admin.like.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "管理端点赞分页结果")
public class AdminLikePageVO<T> {

    @Schema(description = "当前页")
    private long current;

    @Schema(description = "每页条数")
    private long size;

    @Schema(description = "总数")
    private long total;

    @Schema(description = "数据列表")
    private List<T> records;
}