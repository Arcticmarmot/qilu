package com.marmot.qilu.modules.like.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.event.like.LikeEntityType;
import com.marmot.qilu.common.event.like.LikeEvent;
import com.marmot.qilu.common.event.like.LikeProducer;
import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.common.util.ContentUtils;
import com.marmot.qilu.modules.comment.service.PostCommentService;
import com.marmot.qilu.modules.comment.vo.PostCommentPreview;
import com.marmot.qilu.modules.like.entity.Like;
import com.marmot.qilu.modules.like.mapper.LikeMapper;
import com.marmot.qilu.modules.like.service.LikeService;
import com.marmot.qilu.modules.post.service.PostService;
import com.marmot.qilu.modules.post.vo.PostPreview;
import com.marmot.qilu.modules.reply.service.CommentReplyService;
import com.marmot.qilu.modules.reply.vo.CommentReplyPreview;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.marmot.qilu.common.event.like.LikeEntityType.*;
import static java.time.LocalDateTime.now;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private static final int STATUS_UNLIKE = 0;
    private static final int STATUS_LIKE = 1;

    private final LikeMapper postLikeMapper;
    private final PostService postService;
    private final PostCommentService postCommentService;
    private final CommentReplyService commentReplyService;
    private final LikeProducer likeProducer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likePost(Long postId) {
        validateEntityId(postId);

        String currUserUuid = UserContext.requireUuid();

        postService.checkPostInteractable(postId, currUserUuid);

        boolean liked = false;

        Like existing = getLikeByEntity(currUserUuid, postId, POST);

        if (existing == null) {
            Like like = buildLike(currUserUuid, postId, POST);

            try {
                int inserted = postLikeMapper.insert(like);
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
            int updated = updateToLike(currUserUuid, postId, POST);

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
    public void unlikePost(Long postId) {
        validateEntityId(postId);

        String currUserUuid = UserContext.requireUuid();

        postService.checkPostInteractable(postId, currUserUuid);

        int updated = updateToUnlike(currUserUuid, postId, POST);

        if (updated == 1) {
            int rows = postService.decreasePostLikeCount(postId);
            if (rows != 1) {
                throw new IllegalStateException("decrease post like count failed");
            }

            log.info("unlike post success, userUuid={}, postId={}", currUserUuid, postId);
        }
    }

    @Override
    public void likeComment(Long postId, Long commentId) {
        validateEntityId(postId);
        validateEntityId(commentId);

        String currUserUuid = UserContext.requireUuid();

        postCommentService.checkPostCommentInteractable(postId, commentId, currUserUuid);

        boolean liked = false;

        Like existing = getLikeByEntity(currUserUuid, commentId, COMMENT);

        if (existing == null) {
            Like like = buildLike(currUserUuid, commentId, COMMENT);

            try {
                int inserted = postLikeMapper.insert(like);
                if (inserted != 1) {
                    throw new IllegalStateException("like comment failed");
                }

                int rows = postCommentService.increaseCommentLikeCount(commentId);
                if (rows != 1) {
                    throw new IllegalStateException("increase comment like count failed");
                }

                liked = true;
            } catch (DuplicateKeyException e) {
                log.warn("duplicate comment like insert, userUuid={}, postId={}, commentId={}", currUserUuid, postId, commentId);
            }
        }

        if (!liked) {
            int updated = updateToLike(currUserUuid, commentId, COMMENT);

            if (updated == 1) {
                int rows = postCommentService.increaseCommentLikeCount(commentId);
                if (rows != 1) {
                    throw new IllegalStateException("increase post like count failed");
                }

                liked = true;
            }
        }

        if (liked) {
            sendCommentLikeEvent(commentId, currUserUuid);
            log.info("like comment success, userUuid={}, postId={}, commentId={}", currUserUuid, postId, commentId);
        }
    }

    @Override
    public void unlikeComment(Long postId, Long commentId) {
        validateEntityId(postId);
        validateEntityId(commentId);

        String currUserUuid = UserContext.requireUuid();

        postCommentService.checkPostCommentInteractable(postId, commentId, currUserUuid);

        int updated = updateToUnlike(currUserUuid, commentId, COMMENT);

        if (updated == 1) {
            int rows = postCommentService.decreaseCommentLikeCount(commentId);
            if (rows != 1) {
                throw new IllegalStateException("decrease comment like count failed");
            }

            log.info("unlike comment success, userUuid={}, postId={}, commentId={}", currUserUuid, postId, commentId);
        }
    }

    @Override
    public void likeReply(Long postId, Long commentId, Long replyId) {
        validateEntityId(postId);
        validateEntityId(commentId);
        validateEntityId(replyId);

        String currUserUuid = UserContext.requireUuid();
        commentReplyService.checkCommentReplyInteractable(postId, commentId, replyId, currUserUuid);

        boolean liked = false;

        Like existing = getLikeByEntity(currUserUuid, replyId, REPLY);

        if (existing == null) {
            Like like = buildLike(currUserUuid, replyId, REPLY);

            try {
                int inserted = postLikeMapper.insert(like);
                if (inserted != 1) {
                    throw new IllegalStateException("like reply failed");
                }

                int rows = commentReplyService.increaseReplyLikeCount(replyId);
                if (rows != 1) {
                    throw new IllegalStateException("increase reply like count failed");
                }

                liked = true;
            } catch (DuplicateKeyException e) {
                log.warn("duplicate reply like insert, userUuid={}, postId={}, commentId={}, reply={}",
                        currUserUuid, postId, commentId, replyId);
            }
        }

        if (!liked) {
            int updated = updateToLike(currUserUuid, replyId, REPLY);

            if (updated == 1) {
                int rows = commentReplyService.increaseReplyLikeCount(replyId);
                if (rows != 1) {
                    throw new IllegalStateException("increase reply like count failed");
                }

                liked = true;
            }
        }

        if (liked) {
            sendReplyLikeEvent(replyId, currUserUuid);
            log.info("like reply success, userUuid={}, postId={}, commentId={}, reply={}",
                    currUserUuid, postId, commentId, replyId);
        }
    }

    @Override
    public void unlikeReply(Long postId, Long commentId, Long replyId) {
        validateEntityId(postId);
        validateEntityId(commentId);
        validateEntityId(replyId);

        String currUserUuid = UserContext.requireUuid();

        commentReplyService.checkCommentReplyInteractable(postId, commentId, replyId, currUserUuid);

        int updated = updateToUnlike(currUserUuid, commentId, COMMENT);

        if (updated == 1) {
            int rows = commentReplyService.decreaseReplyLikeCount(commentId);
            if (rows != 1) {
                throw new IllegalStateException("decrease reply like count failed");
            }

            log.info("unlike reply success, userUuid={}, postId={}, commentId={}, replyId={}",
                    currUserUuid, postId, commentId, replyId);
        }
    }

    private void sendPostLikeEvent(Long postId, String currUserUuid) {
        PostPreview preview = postService.getPostPreview(postId);
        String receiverUuid = preview.getAuthorUuid();
        String entitySnippet = ContentUtils.buildPostTitlePreview(preview.getTitle());

        if (receiverUuid.equals(currUserUuid)) {
            return;
        }

        LikeEvent event = buildLikeEvent(currUserUuid, receiverUuid, postId, POST, entitySnippet);

        likeProducer.sendLikeEvent(event);
    }

    private void sendCommentLikeEvent(Long commentId, String currUserUuid) {
        PostCommentPreview preview = postCommentService.getPostCommentPreview(commentId);
        String receiverUuid = preview.getAuthorUuid();
        String entitySnippet = ContentUtils.buildCommentContentPreview(preview.getContent());

        if (receiverUuid.equals(currUserUuid)) {
            return;
        }

        LikeEvent event = buildLikeEvent(currUserUuid, receiverUuid, commentId, COMMENT, entitySnippet);

        likeProducer.sendLikeEvent(event);
    }

    private void sendReplyLikeEvent(Long replyId, String currUserUuid) {
        CommentReplyPreview preview = commentReplyService.getCommentReplyPreview(replyId);
        String receiverUuid = preview.getAuthorUuid();
        String entitySnippet = ContentUtils.buildCommentContentPreview(preview.getContent());

        if (receiverUuid.equals(currUserUuid)) {
            return;
        }

        LikeEvent event = buildLikeEvent(currUserUuid, receiverUuid, replyId, REPLY, entitySnippet);

        likeProducer.sendLikeEvent(event);
    }

    private LikeEvent buildLikeEvent(String currUserUuid, String receiverUuid, Long entityId,
                                     LikeEntityType entityType, String entitySnippet) {
        LikeEvent event = new LikeEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setActorUuid(currUserUuid);
        event.setReceiverUuid(receiverUuid);
        event.setEntityType(entityType);
        event.setEntityId(entityId);
        event.setEntitySnippet(entitySnippet);
        event.setOccurredAt(now());
        return event;
    }

    private Like buildLike(String currUserUuid, Long entityId, LikeEntityType entityType) {
        Like like = new Like();
        like.setEntityId(entityId);
        like.setEntityType(entityType.name());
        like.setUserUuid(currUserUuid);
        like.setStatus(STATUS_LIKE);
        return like;
    }

    private Like getLikeByEntity(String currUserUuid, Long entityId, LikeEntityType entityType) {
        return postLikeMapper.selectOne(
                new LambdaQueryWrapper<Like>()
                        .eq(Like::getEntityId, entityId)
                        .eq(Like::getEntityType, entityType.name())
                        .eq(Like::getUserUuid, currUserUuid)
                        .last("limit 1")
        );
    }

    private int updateToLike(String currUserUuid, Long entityId, LikeEntityType entityType) {
        return postLikeMapper.update(
                null,
                new LambdaUpdateWrapper<Like>()
                        .eq(Like::getEntityId, entityId)
                        .eq(Like::getEntityType, entityType.name())
                        .eq(Like::getUserUuid, currUserUuid)
                        .eq(Like::getStatus, STATUS_UNLIKE)
                        .set(Like::getStatus, STATUS_LIKE)
        );
    }

    private int updateToUnlike(String currUserUuid, Long entityId, LikeEntityType entityType) {
        return postLikeMapper.update(
                null,
                new LambdaUpdateWrapper<Like>()
                        .eq(Like::getEntityId, entityId)
                        .eq(Like::getEntityType, entityType.name())
                        .eq(Like::getUserUuid, currUserUuid)
                        .eq(Like::getStatus, STATUS_LIKE)
                        .set(Like::getStatus, STATUS_UNLIKE)
        );
    }



    private void validateEntityId(Long entityId) {
        if(entityId == null || entityId <= 0) {
            throw new BadRequestException("entity id is invalid");
        }
    }

}