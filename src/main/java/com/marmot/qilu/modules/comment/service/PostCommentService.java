package com.marmot.qilu.modules.comment.service;

import com.marmot.qilu.modules.comment.dto.PostCommentCreateDTO;
import com.marmot.qilu.modules.comment.vo.PostCommentPreview;
import com.marmot.qilu.modules.comment.vo.PostCommentListItemVO;

import java.util.List;

public interface PostCommentService {

    void checkPostCommentInteractable(Long postId, Long commentId, String currUserUuid);

    String getAuthorUuid(Long commentId);

    PostCommentPreview getPostCommentPreview(Long commentId);

    void createPostComment(Long postId, PostCommentCreateDTO dto);

    void deletePostComment(Long postId, Long commentId);

    List<PostCommentListItemVO> listPostComments(Long postId);

    int increaseCommentLikeCount(Long commentId);

    int decreaseCommentLikeCount(Long commentId);

    int increaseCommentReplyCount(Long commentId);

    int decreaseCommentReplyCount(Long commentId);
}
