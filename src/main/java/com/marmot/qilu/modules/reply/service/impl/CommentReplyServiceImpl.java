package com.marmot.qilu.modules.reply.service.impl;

import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.event.reply.ReplyEntityType;
import com.marmot.qilu.common.event.reply.ReplyEvent;
import com.marmot.qilu.common.event.reply.ReplyProducer;
import com.marmot.qilu.common.util.ContentUtils;
import com.marmot.qilu.modules.comment.service.PostCommentService;
import com.marmot.qilu.modules.reply.dto.CommentReplyCreateDTO;
import com.marmot.qilu.modules.reply.entity.CommentReply;
import com.marmot.qilu.modules.reply.mapper.CommentReplyMapper;
import com.marmot.qilu.modules.reply.service.CommentReplyService;
import com.marmot.qilu.modules.reply.vo.CommentReplyListItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.marmot.qilu.common.util.ContentUtils.buildCommentContentPreview;
import static java.time.LocalDateTime.now;

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
        if(exists == null) {
            throw new RuntimeException("Reply not interactable.");
        }
    }

    @Override
    public String getAuthorUuidById(Long replyId) {
        String authorUuid = commentReplyMapper.selectUserUuidById(replyId);
        if(authorUuid == null) {
            throw new RuntimeException("Reply not found.");
        }
        return authorUuid;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCommentReply(Long postId, Long commentId,
                                   CommentReplyCreateDTO dto) {
        String currUserUuid = UserContext.requireUuid();

        validateCreateParams(postId, commentId, dto);

        String targetUserUuid;
        Long parentReplyId = dto.getParentReplyId();
        ReplyEvent event = new ReplyEvent();

        if(parentReplyId == null) {
            postCommentService.checkPostCommentInteractable(postId, commentId, currUserUuid);
            targetUserUuid = postCommentService.getAuthorUuidById(commentId);
            event.setEntityType(ReplyEntityType.COMMENT);
            event.setEntityId(commentId);
        } else {
            if(parentReplyId <= 0) {
                throw new RuntimeException("Invalid parentReplyId.");
            }
            checkCommentReplyInteractable(postId, commentId, parentReplyId, currUserUuid);
            targetUserUuid = getAuthorUuidById(parentReplyId);
            event.setEntityType(ReplyEntityType.REPLY);
            event.setEntityId(parentReplyId);
        }

        CommentReply commentReply = new CommentReply();
        String normalizedContent = ContentUtils.normalizeContent(dto.getContent());
        validateContent(normalizedContent);

        commentReply.setStatus(STATUS_NORMAL);
        commentReply.setUserUuid(currUserUuid);
        commentReply.setPostId(postId);
        commentReply.setRootCommentId(commentId);
        commentReply.setContent(normalizedContent);
        commentReply.setParentReplyId(parentReplyId);
        commentReply.setTargetUserUuid(targetUserUuid);

        int inserted = commentReplyMapper.insert(commentReply);
        if(inserted != 1) {
            throw new RuntimeException("Failed to create reply.");
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

    private void validateContentPreview(String preview) {
        if(preview == null || preview.isEmpty()) {
            throw new RuntimeException("Preview cannot be blank.");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCommentReply(Long postId, Long commentId, Long replyId) {
        if(postId == null || postId <= 0) {
            throw new RuntimeException("PostId is invalid.");
        }

        if(commentId == null || commentId <= 0) {
            throw new RuntimeException("CommentId is invalid.");
        }

        if(replyId == null || replyId <= 0) {
            throw new RuntimeException("ReplyId is invalid.");
        }

        String currUserUuid = UserContext.requireUuid();
        postCommentService.checkPostCommentInteractable(postId, commentId, currUserUuid);

        int deleted = commentReplyMapper.deleteCommentReply(replyId, currUserUuid);
        if(deleted != 1) {
            throw new RuntimeException("Reply not found or no permission to delete.");
        }
    }

    @Override
    public List<CommentReplyListItemVO> listCommentReplies(Long postId, Long commentId) {
        String currUserUuid = UserContext.requireUuid();

        postCommentService.checkPostCommentInteractable(postId, commentId, currUserUuid);

        return commentReplyMapper.selectNormalCommentRepliesByCommentId(commentId);
    }

    private void validateCreateParams(Long postId, Long commentId, CommentReplyCreateDTO dto) {
        if(postId == null || postId <= 0) {
            throw new RuntimeException("PostId is invalid.");
        }
        if (commentId == null || commentId <= 0) {
            throw new RuntimeException("RootCommentId is invalid.");
        }
        if (dto == null) {
            throw new RuntimeException("Request body is required.");
        }
    }

    private void validateContent(String content) {
        if (content.isEmpty()) {
            throw new RuntimeException("Content cannot be blank.");
        }
        if (content.length() > MAX_REPLY_CONTENT_LENGTH) {
            throw new RuntimeException("Content too long.");
        }
    }
}
