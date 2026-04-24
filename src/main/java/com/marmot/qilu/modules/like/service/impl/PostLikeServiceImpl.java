package com.marmot.qilu.modules.like.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.event.like.LikeEntityType;
import com.marmot.qilu.common.event.like.LikeEvent;
import com.marmot.qilu.common.event.like.LikeProducer;
import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.modules.like.entity.PostLike;
import com.marmot.qilu.modules.like.mapper.PostLikeMapper;
import com.marmot.qilu.modules.like.service.PostLikeService;
import com.marmot.qilu.modules.post.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static java.time.LocalDateTime.now;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostLikeServiceImpl implements PostLikeService {

    private static final int STATUS_UNLIKED = 0;
    private static final int STATUS_LIKED = 1;

    private final PostLikeMapper postLikeMapper;
    private final PostService postService;
    private final LikeProducer likeProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likePost(Long postId) {
        validatePostId(postId);

        String currUserUuid = UserContext.requireUuid();

        postService.checkPostInteractable(postId, currUserUuid);

        boolean liked = false;

        PostLike existing = postLikeMapper.selectOne(
                new LambdaQueryWrapper<PostLike>()
                        .eq(PostLike::getPostId, postId)
                        .eq(PostLike::getUserUuid, currUserUuid)
                        .last("limit 1")
        );

        if (existing == null) {
            PostLike postLike = new PostLike();
            postLike.setPostId(postId);
            postLike.setUserUuid(currUserUuid);
            postLike.setStatus(STATUS_LIKED);

            try {
                int inserted = postLikeMapper.insert(postLike);
                if (inserted != 1) {
                    throw new IllegalStateException("like post failed");
                }

                int rows = postService.increasePostLikeCount(postId);
                if (rows != 1) {
                    throw new IllegalStateException("increase post like count failed");
                }

                liked = true;
            } catch (DuplicateKeyException e) {
                log.warn("duplicate post like insert, userUuid={}, postId={}", currUserUuid, postId);
            }
        }

        if (!liked) {
            int updated = postLikeMapper.update(
                    null,
                    new LambdaUpdateWrapper<PostLike>()
                            .eq(PostLike::getPostId, postId)
                            .eq(PostLike::getUserUuid, currUserUuid)
                            .eq(PostLike::getStatus, STATUS_UNLIKED)
                            .set(PostLike::getStatus, STATUS_LIKED)
            );

            if (updated == 1) {
                int rows = postService.increasePostLikeCount(postId);
                if (rows != 1) {
                    throw new IllegalStateException("increase post like count failed");
                }

                liked = true;
            }
        }

        if (liked) {
            sendPostLikeEvent(postId, currUserUuid);
            log.info("like post success, userUuid={}, postId={}", currUserUuid, postId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlikePost(Long postId) {
        validatePostId(postId);

        String currUserUuid = UserContext.requireUuid();

        postService.checkPostInteractable(postId, currUserUuid);

        int updated = postLikeMapper.update(
                null,
                new LambdaUpdateWrapper<PostLike>()
                        .eq(PostLike::getPostId, postId)
                        .eq(PostLike::getUserUuid, currUserUuid)
                        .eq(PostLike::getStatus, STATUS_LIKED)
                        .set(PostLike::getStatus, STATUS_UNLIKED)
        );

        if (updated == 1) {
            int rows = postService.decreasePostLikeCount(postId);
            if (rows != 1) {
                throw new IllegalStateException("decrease post like count failed");
            }

            log.info("unlike post success, userUuid={}, postId={}", currUserUuid, postId);
        }
    }

    private void sendPostLikeEvent(Long postId, String currUserUuid) {
        String receiverUuid = postService.getPostAuthorUuid(postId);

        if (receiverUuid.equals(currUserUuid)) {
            return;
        }

        LikeEvent event = new LikeEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEntityType(LikeEntityType.POST);
        event.setEntityId(postId);
        event.setReceiverUuid(receiverUuid);
        event.setActorUuid(currUserUuid);
        event.setOccurredAt(now());

        likeProducer.sendLikeEvent(event);
    }

    private void validatePostId(Long postId) {
        if (postId == null || postId <= 0) {
            throw new BadRequestException("post id is invalid");
        }
    }
}