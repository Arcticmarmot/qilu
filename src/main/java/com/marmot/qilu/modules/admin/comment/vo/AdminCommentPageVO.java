package com.marmot.qilu.modules.admin.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Schema(description = "管理端评论分页结果")
public class AdminCommentPageVO<T> {

    @Schema(description = "当前页")
    private Long current;

    @Schema(description = "每页大小")
    private Long size;

    @Schema(description = "总数")
    private Long total;

    @Schema(description = "评论列表")
    private List<T> records;
}