package com.marmot.qilu.modules.like.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.event.like.LikeEntityType;
import com.marmot.qilu.common.event.like.LikeEvent;
import com.marmot.qilu.common.event.like.LikeProducer;
import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.modules.comment.service.PostCommentService;
import com.marmot.qilu.modules.like.dto.LikeOperateDTO;
import com.marmot.qilu.modules.like.entity.Like;
import com.marmot.qilu.modules.like.mapper.LikeMapper;
import com.marmot.qilu.modules.like.service.LikeService;
import com.marmot.qilu.modules.post.service.PostService;
import com.marmot.qilu.modules.reply.entity.CommentReply;
import com.marmot.qilu.modules.reply.service.CommentReplyService;
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
public class LikeServiceImpl implements LikeService {

    private static final int STATUS_UNLIKED = 0;
    private static final int STATUS_LIKED = 1;

    private final LikeMapper postLikeMapper;
    private final PostService postService;
    private final PostCommentService postCommentService;
    private final CommentReplyService commentReplyService;
    private final LikeProducer likeProducer;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void like(LikeOperateDTO dto) {
        validateLikeOperateDTO(dto);
        String currUserUuid = UserContext.requireUuid();
        Long entityId = dto.getEntityId();
        LikeEntityType entityType = dto.getEntityType();
        switch (entityType) {
            case POST -> likePost(currUserUuid, entityId);
            case COMMENT -> likeComment(currUserUuid, entityId);
            case REPLY -> likeReply(currUserUuid, entityId);
            default -> throw new BadRequestException("entity type is invalid");
        }
    }

    private void likePost(String currUserUuid, Long postId) {
        postService.checkPostInteractable(postId, currUserUuid);

        boolean liked = false;

        Like existing = postLikeMapper.selectOne(
                new LambdaQueryWrapper<Like>()
                        .eq(Like::getEntityId, postId)
                        .eq(Like::getUserUuid, currUserUuid)
                        .last("limit 1")
        );

        if (existing == null) {
            Like postLike = new Like();
            postLike.setEntityId(postId);
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
                    new LambdaUpdateWrapper<Like>()
                            .eq(Like::getEntityId, postId)
                            .eq(Like::getUserUuid, currUserUuid)
                            .eq(Like::getStatus, STATUS_UNLIKED)
                            .set(Like::getStatus, STATUS_LIKED)
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

    private void likeComment(String currUserUuid, Long commentId) {

    }

    private void likeReply(String currUserUuid, Long replyId) {

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlike(LikeOperateDTO dto) {
        validateLikeOperateDTO(dto);

        String currUserUuid = UserContext.requireUuid();
        Long entityId = dto.getEntityId();
        LikeEntityType entityType = dto.getEntityType();

        switch (entityType) {
            case POST -> unlikePost(currUserUuid, entityId);
            case COMMENT -> unlikeComment(currUserUuid, entityId);
            case REPLY -> unlikeReply(currUserUuid, entityId);
            default -> throw new BadRequestException("entity type is invalid");
        }
    }


    private void unlikePost(String currUserUuid, Long postId) {
        postService.checkPostInteractable(postId, currUserUuid);

        int updated = postLikeMapper.update(
                null,
                new LambdaUpdateWrapper<Like>()
                        .eq(Like::getEntityId, postId)
                        .eq(Like::getUserUuid, currUserUuid)
                        .eq(Like::getStatus, STATUS_LIKED)
                        .set(Like::getStatus, STATUS_UNLIKED)
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

    private void unlikeComment(String currUserUuid, Long commentId) {

    }

    private void unlikeReply(String currUserUuid, Long replyId) {

    }

    private void validateLikeOperateDTO(LikeOperateDTO dto) {
        Long entityId = dto.getEntityId();
        LikeEntityType entityType = dto.getEntityType();

        if (entityId == null || entityId <= 0) {
            throw new BadRequestException("entity id is invalid");
        }

        if(entityType == null) {
            throw new BadRequestException("entity type is invalid");
        }
    }
}