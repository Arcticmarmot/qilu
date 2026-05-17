package com.marmot.qilu.modules.admin.reply.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "管理端回复分页项")
public class AdminReplyPageItemVO {

    @Schema(description = "回复ID")
    private Long id;

    @Schema(description = "所属帖子ID")
    private Long postId;

    @Schema(description = "所属根评论ID")
    private Long rootCommentId;

    @Schema(description = "父回复ID")
    private Long parentReplyId;

    @Schema(description = "回复作者用户UUID")
    private String userUuid;

    @Schema(description = "被回复的用户UUID")
    private String targetUserUuid;

    @Schema(description = "回复内容")
    private String content;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "状态：0-删除 1-正常 2-封禁")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}