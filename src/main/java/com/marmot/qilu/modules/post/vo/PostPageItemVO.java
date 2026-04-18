package com.marmot.qilu.modules.post.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "帖子分页项")
public class PostPageItemVO {

    @Schema(description = "帖子ID", example = "101")
    private Long id;

    @Schema(description = "作者用户UUID")
    private String userUuid;

    @Schema(description = "作者昵称", example = "marmot")
    private String nickname;

    @Schema(description = "帖子标题", example = "春天")
    private String title;

    @Schema(description = "正文预览", example = "今天阳光很好...")
    private String contentPreview;

    @Schema(description = "可见性：1公开 2仅自己", example = "1")
    private Integer visibility;

    @Schema(description = "点赞数", example = "3")
    private Integer likeCount;

    @Schema(description = "当前登录用户是否已点赞", example = "false")
    private Boolean likedByMe;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
