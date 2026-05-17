package com.marmot.qilu.modules.admin.reply.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "管理端回复分页查询")
public class AdminReplyPageQueryDTO {

    @Schema(description = "页码，从 1 开始", example = "1")
    @Min(value = 1, message = "current page must be >= 1")
    private long current = 1;

    @Schema(description = "每页条数", example = "10")
    @Min(value = 1, message = "page size must be >= 1")
    private long size = 10;

    @Schema(description = "回复ID", example = "1")
    private Long id;

    @Schema(description = "所属帖子ID", example = "1")
    private Long postId;

    @Schema(description = "所属根评论ID", example = "1")
    private Long rootCommentId;

    @Schema(description = "父回复ID", example = "1")
    private Long parentReplyId;

    @Schema(description = "回复作者用户UUID")
    private String userUuid;

    @Schema(description = "被回复的用户UUID")
    private String targetUserUuid;

    @Schema(description = "回复内容关键词", example = "歧路")
    private String content;

    @Schema(description = "状态：0-删除 1-正常 2-封禁", example = "1")
    private Integer status;
}