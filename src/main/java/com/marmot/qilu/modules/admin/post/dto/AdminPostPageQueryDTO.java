package com.marmot.qilu.modules.admin.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "管理端帖子分页查询")
public class AdminPostPageQueryDTO {

    @Schema(description = "页码，从 1 开始", example = "1")
    @Min(value = 1, message = "current page must be >= 1")
    private long current;

    @Schema(description = "每页条数", example = "10")
    @Min(value = 1, message = "page size must be >= 1")
    private long size;

    @Schema(description = "帖子ID", example = "1")
    private Long postId;

    @Schema(description = "根帖子ID", example = "1")
    private Long rootId;

    @Schema(description = "作者用户UUID", example = "5ee308293252481fb489b21a8d0f8d8d")
    private String userUuid;

    @Schema(description = "标题关键词", example = "歧路")
    private String title;

    @Schema(description = "状态：0-删除 1-正常 2-封禁", example = "1")
    private Integer status;

    @Schema(description = "可见范围：1-公开 2-仅自己", example = "1")
    private Integer visibility;
}
