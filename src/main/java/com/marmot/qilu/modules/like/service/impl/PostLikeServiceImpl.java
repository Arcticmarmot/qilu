package com.marmot.qilu.modules.like.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.modules.like.entity.PostLike;
import com.marmot.qilu.modules.like.mapper.PostLikeMapper;
import com.marmot.qilu.modules.like.service.PostLikeService;
import com.marmot.qilu.modules.like.vo.PostLikeStatusVO;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likePost(Long postId) {
        String currUserUuid = UserContext.getUuid();
        if(currUserUuid == null) {
            throw new RuntimeException("User not logged in.");
        }

        Integer postCount = postLikeMapper.countNormalPostById(postId);
        if(postCount == null || postCount <= 0) {
            throw new RuntimeException("Post not found.");
        }

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
            } catch (DuplicateKeyException e) {
                PostLike duplicated = postLikeMapper.selectOne(
                        new LambdaQueryWrapper<PostLike>()
                                .eq(PostLike::getPostId, postId)
                                .eq(PostLike::getUserUuid, currUserUuid)
                                .last("limit 1")
                );
                if(duplicated != null && STATUS_UNLIKED == duplicated.getStatus()) {
                    int updated = postLikeMapper.update(
                            null,
                            new LambdaQueryWrapper<PostLike>()
                                    .eq(PostLike::getId, duplicated.getId())
                                    .eq(PostLike::getStatus, STATUS_LIKED)
                    );
                    if(updated > 0) {
                        postLikeMapper.increasePostLikeCount(postId);
                    }
                }
            }
            return;
        }

        if(STATUS_LIKED == existing.getStatus()) {
            return;
        }

        int updated = postLikeMapper.update(
                null,
                new LambdaUpdateWrapper<PostLike>()
                        .eq(PostLike::getId, existing.getId())
                        .set(PostLike::getStatus, STATUS_LIKED)
        );

        if(updated > 0) {
            postLikeMapper.increasePostLikeCount(postId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlikePost(Long postId) {
        String currUserUuid = UserContext.getUuid();
        if(currUserUuid == null) {
            throw new RuntimeException("User not logged in.");
        }

        Integer postCount = postLikeMapper.countNormalPostById(postId);
        if(postCount == null || postCount <= 0) {
            throw new RuntimeException("Post not found.");
        }

        PostLike existing = postLikeMapper.selectOne(
                new LambdaQueryWrapper<PostLike>()
                        .eq(PostLike::getPostId, postId)
                        .eq(PostLike::getUserUuid, currUserUuid)
                        .last("limit 1")
        );

        if(existing == null) {
            return;
        }

        if(STATUS_UNLIKED == existing.getStatus()) {
            return;
        }

        int updated = postLikeMapper.update(
                null,
                new LambdaUpdateWrapper<PostLike>()
                        .eq(PostLike::getId, existing.getId())
                        .set(PostLike::getStatus, STATUS_UNLIKED)
        );

        if(updated > 0) {
            postLikeMapper.decreasePostLikeCount(postId);
        }
    }

    @Override
    public PostLikeStatusVO getPostLikeStatus(Long postId) {
        String currUserUuid = UserContext.getUuid();
        if(currUserUuid == null) {
            throw new RuntimeException("User not logged in.");
        }

        Integer postCount = postLikeMapper.countNormalPostById(postId);
        if(postCount == null || postCount <= 0) {
            throw new RuntimeException("Post not found.");
        }

        Integer likeCount = postLikeMapper.selectPostLikeCount(postId);

        PostLike existing = postLikeMapper.selectOne(
                new LambdaQueryWrapper<PostLike>()
                        .eq(PostLike::getPostId, postId)
                        .eq(PostLike::getUserUuid, currUserUuid)
                        .last("limit 1")
        );

        PostLikeStatusVO vo = new PostLikeStatusVO();
        vo.setLikeByMe(existing != null && STATUS_LIKED == existing.getStatus());
        vo.setLikeCount(likeCount == null ? 0 : likeCount);
        return vo;
    }
}
