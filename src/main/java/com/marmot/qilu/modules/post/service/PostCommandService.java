package com.marmot.qilu.modules.post.service;

import com.marmot.qilu.modules.post.dto.*;

public interface PostCommandService {

    void createPost(PostCreateDTO dto);

    void createBranchPost(Long parentPostId, BranchPostCreateDTO dto);

    void updatePost(Long postId, PostUpdateDTO dto);

    void updatePostParent(Long postId, PostParentUpdateDTO dto);

    void deletePost(Long postId);

    int increasePostLikeCount(Long postId);

    int decreasePostLikeCount(Long postId);

    int increasePostCommentCount(Long postId);

    int decreasePostCommentCount(Long postId);
}
