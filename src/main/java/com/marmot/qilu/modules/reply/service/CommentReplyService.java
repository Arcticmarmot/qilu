package com.marmot.qilu.modules.reply.service;

import com.marmot.qilu.modules.reply.dto.CommentReplyCreateDTO;

public interface CommentReplyService{

    void createCommentReply(Long postId, Long rootCommentId, CommentReplyCreateDTO dto);
}
