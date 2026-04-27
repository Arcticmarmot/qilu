package com.marmot.qilu.modules.reply.service.impl;

import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.event.reply.ReplyCreationType;
import com.marmot.qilu.common.event.reply.ReplyEvent;
import com.marmot.qilu.common.event.reply.ReplyProducer;
import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.common.exception.NotFoundException;
import com.marmot.qilu.common.util.ContentUtils;
import com.marmot.qilu.modules.comment.service.CommentService;
import com.marmot.qilu.modules.comment.vo.CommentPreview;
import com.marmot.qilu.modules.reply.dto.ReplyCreateDTO;
import com.marmot.qilu.modules.reply.entity.Reply;
import com.marmot.qilu.modules.reply.mapper.ReplyMapper;
import com.marmot.qilu.modules.reply.service.ReplyService;
import com.marmot.qilu.modules.reply.vo.ReplyListItemVO;
import com.marmot.qilu.modules.reply.vo.ReplyPreview;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.marmot.qilu.common.util.ContentUtils.buildCommentContentSnippet;
import static java.time.LocalDateTime.now;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReplyServiceImpl implements ReplyService {

    private static final int STATUS_NORMAL = 1;
    private static final int MAX_REPLY_CONTENT_LENGTH = 1024;

    private final ReplyMapper replyMapper;
    private final CommentService commentService;
    private final ReplyProducer replyProducer;

    @Override
    public void checkReplyInteractable(Long postId, Long commentId, Long replyId, String currUserUuid) {
        commentService.checkCommentInteractable(postId, commentId, currUserUuid);

        Integer exists = replyMapper.existsInteractableReplyById(postId, commentId, replyId);
        if (exists == null) {
            throw new NotFoundException("reply not found or not interactable");
        }
    }

    @Override
    public String getAuthorUuid(Long replyId) {
        validateCreationId(replyId);

        String authorUuid = replyMapper.selectUserUuidById(replyId);
        if (authorUuid == null) {
            throw new NotFoundException("reply not found");
        }
        return authorUuid;
    }

    @Override
    public ReplyPreview getReplyPreview(Long replyId) {
        validateCreationId(replyId);

        ReplyPreview preview = replyMapper.selectReplyPreviewById(replyId);
        if(preview == null) {
            throw new NotFoundException("reply not found");
        }
        return preview;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createReply(Long postId, Long commentId, ReplyCreateDTO dto) {
        validateCreateParams(postId, commentId, dto);

        String currUserUuid = UserContext.requireUuid();

        String normalizedContent = ContentUtils.normalizeContent(dto.getContent());
        validateContent(normalizedContent);

        Long parentReplyId = dto.getParentReplyId();
        boolean replyToComment = parentReplyId == null;

        String targetUserUuid;
        String creationSnippet;
        ReplyEvent event = new ReplyEvent();

        if (replyToComment) {
            commentService.checkCommentInteractable(postId, commentId, currUserUuid);
            CommentPreview preview  = commentService.getCommentPreview(commentId);
            targetUserUuid = preview.getAuthorUuid();
            creationSnippet = ContentUtils.buildCommentContentSnippet(preview.getContent());

            event.setCreationSnippet(creationSnippet);
            event.setCreationType(ReplyCreationType.COMMENT);
            event.setCreationId(commentId);
        } else {
            validateCreationId(parentReplyId);
            checkReplyInteractable(postId, commentId, parentReplyId, currUserUuid);
            ReplyPreview preview = getReplyPreview(parentReplyId);
            targetUserUuid = preview.getAuthorUuid();
            creationSnippet = ContentUtils.buildCommentContentSnippet(preview.getContent());

            event.setCreationSnippet(creationSnippet);
            event.setCreationType(ReplyCreationType.REPLY);
            event.setCreationId(parentReplyId);
        }

        Reply reply = new Reply();
        reply.setStatus(STATUS_NORMAL);
        reply.setUserUuid(currUserUuid);
        reply.setPostId(postId);
        reply.setRootCommentId(commentId);
        reply.setContent(normalizedContent);
        reply.setParentReplyId(parentReplyId);
        reply.setTargetUserUuid(targetUserUuid);

        int inserted = replyMapper.insert(reply);
        if (inserted != 1) {
            throw new IllegalStateException("create reply failed");
        }

        int rows = commentService.increaseCommentReplyCount(commentId);
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
    public void deleteReply(Long postId, Long commentId, Long replyId) {
        validateCreationId(postId);
        validateCreationId(commentId);
        validateCreationId(replyId);

        String currUserUuid = UserContext.requireUuid();

        checkReplyInteractable(postId, commentId, replyId, currUserUuid);

        int deleted = replyMapper.deleteReply(replyId, currUserUuid);
        if (deleted != 1) {
            throw new NotFoundException("reply not found or no permission");
        }

        int rows = commentService.decreaseCommentReplyCount(commentId);
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
    public List<ReplyListItemVO> listReplies(Long postId, Long commentId) {
        validateCreationId(postId);
        validateCreationId(commentId);

        String currUserUuid = UserContext.requireUuid();

        commentService.checkCommentInteractable(postId, commentId, currUserUuid);

        return replyMapper.selectNormalRepliesByCommentId(currUserUuid, commentId);
    }

    @Override
    public int increaseReplyLikeCount(Long replyId) {
        return replyMapper.increaseReplyLikeCount(replyId);
    }

    @Override
    public int decreaseReplyLikeCount(Long replyId) {
        return replyMapper.decreaseReplyLikeCount(replyId);
    }

    private void sendReplyEvent(ReplyEvent event, Reply reply) {
        if (event == null || reply == null) {
            return;
        }

        if(reply.getUserUuid().equals(reply.getTargetUserUuid())) {
            return;
        }

        String contentSnippet = buildCommentContentSnippet(reply.getContent());
        validateContentSnippet(contentSnippet);

        event.setEventId(UUID.randomUUID().toString());
        event.setReplyId(reply.getId());
        event.setActorUuid(reply.getUserUuid());
        event.setReceiverUuid(reply.getTargetUserUuid());
        event.setContentSnippet(contentSnippet);
        event.setOccurredAt(now());

        replyProducer.sendReplyEvent(event);
    }

    private void validateCreateParams(Long postId, Long commentId, ReplyCreateDTO dto) {
        validateCreationId(postId);
        validateCreationId(commentId);

        if (dto == null) {
            throw new BadRequestException("request body must not be null");
        }
    }

    private void validateCreationId(Long creationId) {
        if (creationId == null || creationId <= 0) {
            throw new BadRequestException("creation id is invalid");
        }
    }

    private void validateContentSnippet(String preview) {
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