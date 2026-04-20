package com.marmot.qilu.modules.comment.service.impl;

import com.marmot.qilu.modules.comment.dto.PostCommentCreateDTO;
import com.marmot.qilu.modules.comment.mapper.PostCommentMapper;
import com.marmot.qilu.modules.comment.service.PostCommentService;
import com.marmot.qilu.modules.comment.vo.PostCommentListItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostCommentServiceImpl implements PostCommentService {

    private final PostCommentMapper postCommentMapper;

    @Override
    public void createPostComment(Long postId, PostCommentCreateDTO dto) {

    }

    @Override
    public void deletePostComment(Long commentId) {

    }

    @Override
    public List<PostCommentListItemVO> listPostComments(Long postId) {
        return List.of();
    }
}
