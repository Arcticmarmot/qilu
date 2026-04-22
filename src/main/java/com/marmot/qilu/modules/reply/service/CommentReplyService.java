package com.marmot.qilu.modules.reply.service;

import com.marmot.qilu.modules.reply.dto.CommentReplyCreateDTO;
import com.marmot.qilu.modules.reply.vo.CommentReplyListItemVO;

import java.util.List;

public interface CommentReplyService {

    void checkCommentReplyInteractable(Long postId, Long commentId,
                                       Long replyId, String currUserUuid);

    String getAuthorUuidById(Long replyId);

    void createCommentReply(Long postId, Long commentId, CommentReplyCreateDTO dto);

    List<CommentReplyListItemVO> listCommentReplies(Long postId, Long commentId);
}
