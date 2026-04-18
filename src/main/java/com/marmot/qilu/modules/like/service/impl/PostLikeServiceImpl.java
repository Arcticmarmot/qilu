package com.marmot.qilu.modules.like.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.modules.like.entity.PostLike;
import com.marmot.qilu.modules.like.mapper.PostLikeMapper;
import com.marmot.qilu.modules.like.service.PostLikeService;
import com.marmot.qilu.modules.post.service.PostService;
import com.marmot.qilu.modules.post.service.impl.PostServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostLikeServiceImpl implements PostLikeService {

    private static final int STATUS_UNLIKED = 0;
    private static final int STATUS_LIKED = 1;

    private final PostLikeMapper postLikeMapper;
    private final PostService postService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likePost(Long postId) {
        String currUserUuid = UserContext.requireUuid();

        postService.checkPostInteractable(postId);

        PostLike existing = postLikeMapper.selectOne(
                new LambdaQueryWrapper<PostLike>()
                        .eq(PostLike::getPostId, postId)
                        .eq(PostLike::getUserUuid, currUserUuid)
                        .last("limit 1")
        );

        if(existing == null) {
            PostLike postLike = new PostLike();
            postLike.setPostId(postId);
            postLike.setUserUuid(currUserUuid);
            postLike.setStatus(STATUS_LIKED);

            try {
                postLikeMapper.insert(postLike);
                postLikeMapper.increasePostLikeCount(postId);
                return;
            } catch (DuplicateKeyException ignored) { }
        }

        int updated = postLikeMapper.update(
                null,
                new LambdaUpdateWrapper<PostLike>()
                        .eq(PostLike::getPostId, postId)
                        .eq(PostLike::getUserUuid, currUserUuid)
                        .eq(PostLike::getStatus, STATUS_UNLIKED)
                        .set(PostLike::getStatus, STATUS_LIKED)
        );

        if (updated > 0) {
            int rows = postLikeMapper.increasePostLikeCount(postId);
            if (rows <= 0) {
                throw new RuntimeException("Like post failed.");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlikePost(Long postId) {
        String currUserUuid = UserContext.requireUuid();

        postService.checkPostInteractable(postId);

        int updated = postLikeMapper.update(
                null,
                new LambdaUpdateWrapper<PostLike>()
                        .eq(PostLike::getPostId, postId)
                        .eq(PostLike::getUserUuid, currUserUuid)
                        .eq(PostLike::getStatus, STATUS_LIKED)
                        .set(PostLike::getStatus, STATUS_UNLIKED)
        );

        if(updated > 0) {
            int rows = postLikeMapper.decreasePostLikeCount(postId);
            if(rows <= 0) {
                throw new RuntimeException("Unlike post failed");
            }
        }
    }
}
