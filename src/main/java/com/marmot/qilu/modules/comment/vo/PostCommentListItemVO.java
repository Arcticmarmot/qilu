package com.marmot.qilu.modules.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "帖子评论列表项")
public class PostCommentListItemVO {

    @Schema(description = "评论ID", example = "1")
    private Long id;

    @Schema(description = "帖子ID", example = "1001")
    private Long postId;

    @Schema(description = "评论者用户UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String userUuid;

    @Schema(description = "评论者昵称", example = "marmot")
    private String nickname;

    @Schema(description = "评论内容", example = "写得很好")
    private String content;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
