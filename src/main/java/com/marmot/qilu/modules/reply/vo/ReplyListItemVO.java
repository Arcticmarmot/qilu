package com.marmot.qilu.modules.reply.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "评论回复列表项")
public class ReplyListItemVO {

    @Schema(description = "回复ID", example = "1")
    private Long id;

    @Schema(description = "根评论ID", example = "1001")
    private Long rootCommentId;

    @Schema(description = "上级回复ID", example = "1001")
    private Long parentReplyId;

    @Schema(description = "回复者UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String userUuid;

    @Schema(description = "回复者昵称", example = "marmot")
    private String nickname;

    @Schema(description = "被回复者UUID", example = "550e8400-e29b-41d4-a716-446655440000")
    private String targetUserUuid;

    @Schema(description = "被回复者昵称", example = "marmot")
    private String targetNickname;

    @Schema(description = "评论内容", example = "写得很好")
    private String content;

    @Schema(description = "回复点赞数", example = "1")
    private Integer likeCount;

    @Schema(description = "当前登录用户是否已点赞", example = "false")
    private Boolean likedByMe;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
