package com.marmot.qilu.modules.reply.service.impl;

import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.event.reply.ReplyEntityType;
import com.marmot.qilu.common.event.reply.ReplyEvent;
import com.marmot.qilu.common.event.reply.ReplyProducer;
import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.common.exception.NotFoundException;
import com.marmot.qilu.common.util.ContentUtils;
import com.marmot.qilu.modules.comment.service.PostCommentService;
import com.marmot.qilu.modules.comment.vo.PostCommentPreview;
import com.marmot.qilu.modules.reply.dto.CommentReplyCreateDTO;
import com.marmot.qilu.modules.reply.entity.CommentReply;
import com.marmot.qilu.modules.reply.mapper.CommentReplyMapper;
import com.marmot.qilu.modules.reply.service.CommentReplyService;
import com.marmot.qilu.modules.reply.vo.CommentReplyListItemVO;
import com.marmot.qilu.modules.reply.vo.CommentReplyPreview;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.marmot.qilu.common.util.ContentUtils.buildCommentContentPreview;
import static java.time.LocalDateTime.now;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentReplyServiceImpl implements CommentReplyService {

    private static final int STATUS_NORMAL = 1;
    private static final int MAX_REPLY_CONTENT_LENGTH = 1024;

    private final CommentReplyMapper commentReplyMapper;
    private final PostCommentService postCommentService;
    private final ReplyProducer replyProducer;

    @Override
    public void checkCommentReplyInteractable(Long postId, Long commentId, Long replyId, String currUserUuid) {
        postCommentService.checkPostCommentInteractable(postId, commentId, currUserUuid);

        Integer exists = commentReplyMapper.existsInteractableCommentReplyById(postId, commentId, replyId);
        if (exists == null) {
            throw new NotFoundException("reply not found or not interactable");
        }
    }

    @Override
    public String getAuthorUuid(Long replyId) {
        validateEntityId(replyId);

        String authorUuid = commentReplyMapper.selectUserUuidById(replyId);
        if (authorUuid == null) {
            throw new NotFoundException("reply not found");
        }
        return authorUuid;
    }

    @Override
    public CommentReplyPreview getCommentReplyPreview(Long replyId) {
        validateEntityId(replyId);

        CommentReplyPreview preview = commentReplyMapper.selectCommentReplyPreviewById(replyId);
        if(preview == null) {
            throw new NotFoundException("reply not found");
        }
        return preview;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCommentReply(Long postId, Long commentId, CommentReplyCreateDTO dto) {
        validateCreateParams(postId, commentId, dto);

        String currUserUuid = UserContext.requireUuid();

        String normalizedContent = ContentUtils.normalizeContent(dto.getContent());
        validateContent(normalizedContent);

        Long parentReplyId = dto.getParentReplyId();
        boolean replyToComment = parentReplyId == null;

        String targetUserUuid;
        String entitySnippet;
        ReplyEvent event = new ReplyEvent();

        if (replyToComment) {
            postCommentService.checkPostCommentInteractable(postId, commentId, currUserUuid);
            PostCommentPreview preview  = postCommentService.getPostCommentPreview(commentId);
            targetUserUuid = preview.getAuthorUuid();
            entitySnippet = ContentUtils.buildCommentContentPreview(preview.getContent());

            event.setEntitySnippet(entitySnippet);
            event.setEntityType(ReplyEntityType.COMMENT);
            event.setEntityId(commentId);
        } else {
            validateEntityId(parentReplyId);
            checkCommentReplyInteractable(postId, commentId, parentReplyId, currUserUuid);
            CommentReplyPreview preview = getCommentReplyPreview(parentReplyId);
            targetUserUuid = preview.getAuthorUuid();
            entitySnippet = ContentUtils.buildCommentContentPreview(preview.getContent());

            event.setEntitySnippet(entitySnippet);
            event.setEntityType(ReplyEntityType.REPLY);
            event.setEntityId(parentReplyId);
        }

        CommentReply reply = new CommentReply();
        reply.setStatus(STATUS_NORMAL);
        reply.setUserUuid(currUserUuid);
        reply.setPostId(postId);
        reply.setRootCommentId(commentId);
        reply.setContent(normalizedContent);
        reply.setParentReplyId(parentReplyId);
        reply.setTargetUserUuid(targetUserUuid);

        int inserted = commentReplyMapper.insert(reply);
        if (inserted != 1) {
            throw new IllegalStateException("create reply failed");
        }

        int rows = postCommentService.increaseCommentReplyCount(commentId);
        if (rows != 1) {
            throw new IllegalStateException("increase comment reply count failed");
        }

        sendReplyEvent(event, reply);

        log.info(
                "create comment reply success, userUuid={}, postId={}, commentId={}, replyId={}, parentReplyId={}",
                currUserUuid,
                postId,
                commentId,
                reply.getId(),
                parentReplyId
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCommentReply(Long postId, Long commentId, Long replyId) {
        validateEntityId(postId);
        validateEntityId(commentId);
        validateEntityId(replyId);

        String currUserUuid = UserContext.requireUuid();

        checkCommentReplyInteractable(postId, commentId, replyId, currUserUuid);

        int deleted = commentReplyMapper.deleteCommentReply(replyId, currUserUuid);
        if (deleted != 1) {
            throw new NotFoundException("reply not found or no permission");
        }

        int rows = postCommentService.decreaseCommentReplyCount(commentId);
        if(rows != 1) {
            throw new IllegalStateException("delete reply failed");
        }

        log.info(
                "delete comment reply success, userUuid={}, postId={}, commentId={}, replyId={}",
                currUserUuid,
                postId,
                commentId,
                replyId
        );
    }

    @Override
    public List<CommentReplyListItemVO> listCommentReplies(Long postId, Long commentId) {
        validateEntityId(postId);
        validateEntityId(commentId);

        String currUserUuid = UserContext.requireUuid();

        postCommentService.checkPostCommentInteractable(postId, commentId, currUserUuid);

        return commentReplyMapper.selectNormalCommentRepliesByCommentId(currUserUuid, commentId);
    }

    @Override
    public int increaseReplyLikeCount(Long replyId) {
        return commentReplyMapper.increaseReplyLikeCount(replyId);
    }

    @Override
    public int decreaseReplyLikeCount(Long replyId) {
        return commentReplyMapper.decreaseReplyLikeCount(replyId);
    }

    private void sendReplyEvent(ReplyEvent event, CommentReply commentReply) {
        if (event == null || commentReply == null) {
            return;
        }

        if(commentReply.getUserUuid().equals(commentReply.getTargetUserUuid())) {
            return;
        }

        String contentPreview = buildCommentContentPreview(commentReply.getContent());
        validateContentPreview(contentPreview);

        event.setEventId(UUID.randomUUID().toString());
        event.setReplyId(commentReply.getId());
        event.setActorUuid(commentReply.getUserUuid());
        event.setReceiverUuid(commentReply.getTargetUserUuid());
        event.setContentPreview(contentPreview);
        event.setOccurredAt(now());

        replyProducer.sendReplyEvent(event);
    }

    private void validateCreateParams(Long postId, Long commentId, CommentReplyCreateDTO dto) {
        validateEntityId(postId);
        validateEntityId(commentId);

        if (dto == null) {
            throw new BadRequestException("request body must not be null");
        }
    }

    private void validateEntityId(Long entityId) {
        if (entityId == null || entityId <= 0) {
            throw new BadRequestException("entity id is invalid");
        }
    }

    private void validateContentPreview(String preview) {
        if (preview == null || preview.isEmpty()) {
            throw new BadRequestException("preview cannot be blank");
        }
    }

    private void validateContent(String content) {
        if (content == null || content.isEmpty()) {
            throw new BadRequestException("content cannot be blank");
        }

        if (content.length() > MAX_REPLY_CONTENT_LENGTH) {
            throw new BadRequestException("content too long");
        }
    }
}