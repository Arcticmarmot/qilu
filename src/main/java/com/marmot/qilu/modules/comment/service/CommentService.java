package com.marmot.qilu.modules.comment.service;

import com.marmot.qilu.modules.comment.dto.CommentCreateDTO;
import com.marmot.qilu.modules.comment.vo.CommentPreview;
import com.marmot.qilu.modules.comment.vo.CommentListItemVO;

import java.util.List;

public interface CommentService {

    void checkCommentInteractable(Long postId, Long commentId, String currUserUuid);

    String getAuthorUuid(Long commentId);

    CommentPreview getCommentPreview(Long commentId);

    void createComment(Long postId, CommentCreateDTO dto);

    void deleteComment(Long postId, Long commentId);

    List<CommentListItemVO> listComments(Long postId);

    int increaseCommentLikeCount(Long commentId);

    int decreaseCommentLikeCount(Long commentId);

    int increaseCommentReplyCount(Long commentId);

    int decreaseCommentReplyCount(Long commentId);
}
