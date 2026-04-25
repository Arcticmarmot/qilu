package com.marmot.qilu.modules.reply.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentReplyInfoVO {

    private Long id;

    private Long postId;

    private Long rootCommentId;

    private Long parentReplyId;

    private String userUuid;

    private String targetUserUuid;
}
