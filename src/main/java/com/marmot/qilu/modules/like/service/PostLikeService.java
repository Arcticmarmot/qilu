package com.marmot.qilu.modules.like.service;

public interface PostLikeService {

    void likePost(Long postId);

    void unlikePost(Long postId);
}
