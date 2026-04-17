package com.marmot.qilu.modules.like.service.impl;

import com.marmot.qilu.modules.like.service.PostLikeService;
import com.marmot.qilu.modules.like.vo.PostLikeStatusVO;
import org.springframework.stereotype.Service;

@Service
public class PostLikeServiceImpl implements PostLikeService {
    @Override
    public void likePost(Long postId) {

    }

    @Override
    public void unlikePost(Long postId) {

    }

    @Override
    public PostLikeStatusVO getPostLikeStatus(Long postId) {
        return null;
    }
}
