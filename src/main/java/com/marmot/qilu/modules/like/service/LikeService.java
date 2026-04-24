package com.marmot.qilu.modules.like.service;

public interface LikeService {

    void likePost(Long postId);

    void unlikePost(Long postId);

    void likeComment(Long postId, Long commentId);

    void unlikeComment(Long postId, Long commentId);

    void likeReply(Long postId, Long commentId, Long replyId);

    void unlikeReply(Long postId, Long commentId, Long replyId);
}