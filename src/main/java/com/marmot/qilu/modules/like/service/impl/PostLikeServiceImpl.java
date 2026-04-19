package com.marmot.qilu.modules.like.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.event.interaction.InteractionEntityType;
import com.marmot.qilu.common.event.interaction.InteractionEvent;
import com.marmot.qilu.common.event.interaction.InteractionEventProducer;
import com.marmot.qilu.common.event.interaction.InteractionEventType;
import com.marmot.qilu.modules.like.entity.PostLike;
import com.marmot.qilu.modules.like.mapper.PostLikeMapper;
import com.marmot.qilu.modules.like.service.PostLikeService;
import com.marmot.qilu.modules.post.mapper.PostMapper;
import com.marmot.qilu.modules.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static java.time.LocalDateTime.now;

@Service
@RequiredArgsConstructor
public class PostLikeServiceImpl implements PostLikeService {

    private static final int STATUS_UNLIKED = 0;
    private static final int STATUS_LIKED = 1;

    private final PostLikeMapper postLikeMapper;
    private final PostService postService;
    private final PostMapper postMapper;
    private final InteractionEventProducer interactionEventProducer;

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

        // kafka post-liked event
        String receiverUuid = postMapper.getUserUuidByPostId(postId);
        if(receiverUuid == null || receiverUuid.equals(currUserUuid)) {
            return;
        }
        InteractionEvent event = new InteractionEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setEventType(InteractionEventType.POST_LIKED);
        event.setEntityType(InteractionEntityType.POST);
        event.setEntityId(postId);
        event.setReceiverUuid(receiverUuid);
        event.setActorUuid(currUserUuid);
        event.setOccurredAt(now());

        interactionEventProducer.sendInteractionEvent(event);
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
