package com.marmot.qilu.modules.admin.post.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "管理端帖子分页结果")
public class AdminPostPageVO<T> {

    @Schema(description = "当前页")
    private long current;

    @Schema(description = "每页条数")
    private long size;

    @Schema(description = "总数")
    private long total;

    @Schema(description = "数据列表")
    private List<T> records;
}
