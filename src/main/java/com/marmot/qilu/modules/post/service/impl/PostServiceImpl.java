package com.marmot.qilu.modules.post.service.impl;

import com.marmot.qilu.modules.post.dto.*;
import com.marmot.qilu.modules.post.model.PostPreview;
import com.marmot.qilu.modules.post.model.PostSearchSource;
import com.marmot.qilu.modules.post.service.PostCommandService;
import com.marmot.qilu.modules.post.service.PostQueryService;
import com.marmot.qilu.modules.post.service.PostService;
import com.marmot.qilu.modules.post.vo.PostDetailVO;
import com.marmot.qilu.modules.post.vo.PostPageItemVO;
import com.marmot.qilu.modules.post.vo.PostPageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostCommandService postCommandService;
    private final PostQueryService postQueryService;

    @Override
    public void createPost(PostCreateDTO dto) {
        postCommandService.createPost(dto);
    }

    @Override
    public void createBranchPost(Long parentPostId, BranchPostCreateDTO dto) {
        postCommandService.createBranchPost(parentPostId, dto);
    }

    @Override
    public void updatePost(Long postId, PostUpdateDTO dto) {
        postCommandService.updatePost(postId, dto);
    }

    @Override
    public void updatePostParent(Long postId, PostParentUpdateDTO dto) {
        postCommandService.updatePostParent(postId, dto);
    }

    @Override
    public void deletePost(Long postId) {
        postCommandService.deletePost(postId);
    }

    @Override
    public void checkPostInteractable(Long postId, String currUserUuid) {
        postQueryService.checkPostInteractable(postId, currUserUuid);
    }

    @Override
    public List<PostSearchSource> getPostSearchSourceList(List<Long> postIds) {
        return postQueryService.getPostSearchSourceList(postIds);
    }

    @Override
    public List<PostPageItemVO> getPublicPostsByIds(List<Long> postIds) {
        return postQueryService.getPublicPostsByIds(postIds);
    }

    @Override
    public Map<Long, LocalDateTime> getPublicPostCreatedAtMapByIds(List<Long> postIds) {
        return postQueryService.getPublicPostCreatedAtMapByIds(postIds);
    }

    @Override
    public PostPreview getPostPreview(Long postId) {
        return postQueryService.getPostPreview(postId);
    }

    @Override
    public List<PostDetailVO> getMyPostDetail(Long postId) {
        return postQueryService.getMyPostDetail(postId);
    }

    @Override
    public List<PostDetailVO> getPublicPostDetail(Long postId) {
        return postQueryService.getPublicPostDetail(postId);
    }

    @Override
    public PostPageVO<PostPageItemVO> getMyPostPage(PostPageQueryDTO dto) {
        return postQueryService.getMyPostPage(dto);
    }

    @Override
    public PostPageVO<PostPageItemVO> getPublicPostPage(PostPageQueryDTO dto) {
        return postQueryService.getPublicPostPage(dto);
    }

    @Override
    public int increasePostLikeCount(Long postId) {
        return postCommandService.increasePostLikeCount(postId);
    }

    @Override
    public int decreasePostLikeCount(Long postId) {
        return postCommandService.decreasePostLikeCount(postId);
    }

    @Override
    public int increasePostCommentCount(Long postId) {
        return postCommandService.increasePostCommentCount(postId);
    }

    @Override
    public int decreasePostCommentCount(Long postId) {
        return postCommandService.decreasePostCommentCount(postId);
    }
}