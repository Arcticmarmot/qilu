package com.marmot.qilu.modules.admin.post.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "管理端帖子分页项")
public class AdminPostPageItemVO {

    @Schema(description = "帖子ID")
    private Long id;

    @Schema(description = "父帖子ID")
    private Long parentId;

    @Schema(description = "根帖子ID")
    private Long rootId;

    @Schema(description = "分支对话内容")
    private String branchPrompt;

    @Schema(description = "作者用户UUID")
    private String userUuid;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "内容预览")
    private String contentSnippet;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "状态：0-删除 1-正常 2-封禁")
    private Integer status;

    @Schema(description = "可见范围：1-公开 2-仅自己")
    private Integer visibility;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "软删除时间")
    private LocalDateTime deletedAt;
}
