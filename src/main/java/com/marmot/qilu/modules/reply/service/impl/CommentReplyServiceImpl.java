package com.marmot.qilu.modules.reply.service.impl;

import com.marmot.qilu.common.context.UserContext;
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
        reply.setStatus(STATUS_NORMAL);
        reply.setUserUuid(currUserUuid);
        reply.setPostId(postId);
        reply.setRootCommentId(commentId);
        reply.setContent(dto.getContent());
        reply.setParentReplyId(parentReplyId);
        reply.setTargetUserUuid(targetUserUuid);

        int inserted = commentReplyMapper.insert(reply);
        if(inserted != 1) {
            throw new RuntimeException("Failed to create reply.");
        }

        // TODO: kafka notification
    }

    @Override
    public List<CommentReplyListItemVO> listCommentReplies(Long postId, Long commentId) {
        String currUserUuid = UserContext.requireUuid();

        postCommentService.checkPostCommentInteractable(postId, commentId, currUserUuid);

        return commentReplyMapper.selectNormalCommentRepliesByCommentId(commentId);
    }

    private void validateCreateParams(Long postId, Long rootCommentId, CommentReplyCreateDTO dto) {
        if(postId == null || postId <= 0) {
            throw new RuntimeException("PostId is invalid.");
        }
        if (rootCommentId == null || rootCommentId <= 0) {
            throw new RuntimeException("RootCommentId is invalid.");
        }
        if (dto == null) {
            throw new RuntimeException("Request body is required.");
        }
    }
}
