package com.marmot.qilu.modules.reply.service;

import com.marmot.qilu.modules.reply.dto.ReplyCreateDTO;
import com.marmot.qilu.modules.reply.vo.ReplyListItemVO;
import com.marmot.qilu.modules.reply.vo.ReplyPreview;

import java.util.List;

public interface ReplyService {

    void checkReplyInteractable(Long postId, Long commentId,
                                Long replyId, String currUserUuid);

    String getAuthorUuid(Long replyId);

    ReplyPreview getReplyPreview(Long replyId);

    void createReply(Long postId, Long commentId, ReplyCreateDTO dto);

    void deleteReply(Long postId, Long commentId, Long replyId);

    List<ReplyListItemVO> listReplies(Long postId, Long commentId);

    int increaseReplyLikeCount(Long replyId);

    int decreaseReplyLikeCount(Long replyId);
}
