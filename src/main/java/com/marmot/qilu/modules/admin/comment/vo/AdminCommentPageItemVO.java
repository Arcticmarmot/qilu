package com.marmot.qilu.modules.admin.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "管理端评论分页项")
public class AdminCommentPageItemVO {

    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "被评论的帖子ID")
    private Long postId;

    @Schema(description = "帖子作者用户UUID")
    private String postAuthorUuid;

    @Schema(description = "评论者用户UUID")
    private String userUuid;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "回复数")
    private Integer replyCount;

    @Schema(description = "状态：0-删除 1-正常 2-封禁")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}