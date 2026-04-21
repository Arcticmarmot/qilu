package com.marmot.qilu.modules.reply.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "评论回复列表项")
public class CommentReplyListItemVO {

    private Long id;

    private Long rootCommentId;

    private Long parentReplyId;

    private String userUuid;

    private String nickname;

    private String targetUserUuid;

    private String targetNickname;

    private String content;

    private LocalDateTime createdAt;
}
