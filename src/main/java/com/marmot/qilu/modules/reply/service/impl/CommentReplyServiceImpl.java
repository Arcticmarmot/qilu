package com.marmot.qilu.modules.reply.service.impl;

import com.marmot.qilu.common.context.UserContext;
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

@Service
@RequiredArgsConstructor
public class CommentReplyServiceImpl implements CommentReplyService {

    private static final int STATUS_NORMAL = 1;
    private static final int MAX_REPLY_CONTENT_LENGTH = 1024;

    private final CommentReplyMapper commentReplyMapper;
    private final PostCommentService postCommentService;

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
        if(parentReplyId == null) {
            postCommentService.checkPostCommentInteractable(postId, commentId, currUserUuid);
            targetUserUuid = postCommentService.getAuthorUuidById(commentId);
        } else {
            if(parentReplyId <= 0) {
                throw new RuntimeException("Invalid parentReplyId.");
            }
            checkCommentReplyInteractable(postId, commentId, parentReplyId, currUserUuid);
            targetUserUuid = getAuthorUuidById(parentReplyId);
        }

        CommentReply reply = new CommentReply();
        String normalizedContent = ContentUtils.normalizeContent(dto.getContent());
        validateContent(normalizedContent);

        reply.setStatus(STATUS_NORMAL);
        reply.setUserUuid(currUserUuid);
        reply.setPostId(postId);
        reply.setRootCommentId(commentId);
        reply.setContent(normalizedContent);
        reply.setParentReplyId(parentReplyId);
        reply.setTargetUserUuid(targetUserUuid);

        int inserted = commentReplyMapper.insert(reply);
        if(inserted != 1) {
            throw new RuntimeException("Failed to create reply.");
        }

        // TODO: kafka notification
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
