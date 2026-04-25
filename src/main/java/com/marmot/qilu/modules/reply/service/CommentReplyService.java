package com.marmot.qilu.modules.reply.service;

import com.marmot.qilu.modules.reply.dto.CommentReplyCreateDTO;
import com.marmot.qilu.modules.reply.vo.CommentReplyListItemVO;

import java.util.List;

public interface CommentReplyService {

    void checkCommentReplyInteractable(Long postId, Long commentId,
                                       Long replyId, String currUserUuid);

    String getAuthorUuidById(Long replyId);

    void createCommentReply(Long postId, Long commentId, CommentReplyCreateDTO dto);

    void deleteCommentReply(Long postId, Long commentId, Long replyId);

    List<CommentReplyListItemVO> listCommentReplies(Long postId, Long commentId);

    int increaseReplyLikeCount(Long replyId);

    int decreaseReplyLikeCount(Long replyId);
}
