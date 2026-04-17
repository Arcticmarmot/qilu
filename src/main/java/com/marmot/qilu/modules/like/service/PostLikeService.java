package com.marmot.qilu.modules.like.service;

import com.marmot.qilu.modules.like.vo.PostLikeStatusVO;

public interface PostLikeService {

    void likePost(Long postId);

    void unlikePost(Long postId);

    PostLikeStatusVO getPostLikeStatus(Long postId);
}
