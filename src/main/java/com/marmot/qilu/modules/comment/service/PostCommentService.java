package com.marmot.qilu.modules.comment.service;

import com.marmot.qilu.modules.comment.dto.PostCommentCreateDTO;
import com.marmot.qilu.modules.comment.vo.PostCommentListItemVO;

import java.util.List;

public interface PostCommentService {

    void createPostComment(Long postId, PostCommentCreateDTO dto);

    void deletePostComment(Long commentId);

    List<PostCommentListItemVO> listPostComments(Long postId);
}
