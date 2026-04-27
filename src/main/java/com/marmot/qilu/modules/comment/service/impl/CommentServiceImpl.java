package com.marmot.qilu.modules.comment.service.impl;

import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.event.comment.CommentEvent;
import com.marmot.qilu.common.event.comment.CommentProducer;
import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.common.exception.NotFoundException;
import com.marmot.qilu.common.util.ContentUtils;
import com.marmot.qilu.modules.comment.dto.CommentCreateDTO;
import com.marmot.qilu.modules.comment.entity.Comment;
import com.marmot.qilu.modules.comment.mapper.CommentMapper;
import com.marmot.qilu.modules.comment.service.CommentService;
import com.marmot.qilu.modules.comment.vo.CommentPreview;
import com.marmot.qilu.modules.comment.vo.CommentListItemVO;
import com.marmot.qilu.modules.post.service.PostService;
import com.marmot.qilu.modules.post.vo.PostPreview;
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
public class CommentServiceImpl implements CommentService {

    private static final int STATUS_NORMAL = 1;
    private static final int MAX_COMMENT_CONTENT_LENGTH = 1024;

    private final CommentMapper commentMapper;
    private final PostService postService;
    private final CommentProducer commentProducer;

    @Override
    public void checkCommentInteractable(Long postId, Long commentId, String currUserUuid) {
        postService.checkPostInteractable(postId, currUserUuid);

        Integer exists = commentMapper.existsInteractableCommentById(postId, commentId);
        if (exists == null) {
            throw new NotFoundException("comment not found or not interactable");
        }
    }

    @Override
    public String getAuthorUuid(Long commentId) {
        if (commentId == null || commentId <= 0) {
            throw new BadRequestException("comment id is invalid");
        }

        String authorUuid = commentMapper.selectUserUuidById(commentId);
        if (authorUuid == null) {
            throw new NotFoundException("comment not found");
        }
        return authorUuid;
    }

    @Override
    public CommentPreview getCommentPreview(Long commentId) {
        if (commentId == null || commentId <= 0) {
            throw new BadRequestException("comment id is invalid");
        }

        CommentPreview preview = commentMapper.selectCommentPreviewById(commentId);
        if(preview == null) {
            throw new NotFoundException("comment not found");
        }
        return preview;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createComment(Long postId, CommentCreateDTO dto) {
        validatePostId(postId);

        if (dto == null) {
            throw new BadRequestException("request body must not be null");
        }

        String currUserUuid = UserContext.requireUuid();

        postService.checkPostInteractable(postId, currUserUuid);

        PostPreview preview = postService.getPostPreview(postId);
        String postAuthorUuid = preview.getAuthorUuid();
        String postSnippet = ContentUtils.buildPostTitlePreview(preview.getTitle());

        String normalizedContent = ContentUtils.normalizeContent(dto.getContent());
        validateContent(normalizedContent);

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setPostAuthorUuid(postAuthorUuid);
        comment.setUserUuid(currUserUuid);
        comment.setContent(normalizedContent);
        comment.setStatus(STATUS_NORMAL);

        int inserted = commentMapper.insert(comment);
        if (inserted != 1) {
            throw new IllegalStateException("create comment failed");
        }

        int rows = postService.increasePostCommentCount(postId);
        if (rows != 1) {
            throw new IllegalStateException("increase post comment count failed");
        }

        sendCommentEvent(comment, postSnippet);

        log.info(
                "create post comment success, userUuid={}, postId={}, commentId={}",
                currUserUuid,
                postId,
                comment.getId()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long postId, Long commentId) {
        validatePostId(postId);
        validateCommentId(commentId);

        String currUserUuid = UserContext.requireUuid();

        checkCommentInteractable(postId, commentId, currUserUuid);

        int deleted = commentMapper.deleteComment(commentId, currUserUuid);
        if (deleted != 1) {
            throw new NotFoundException("comment not found or no permission");
        }

        int rows = postService.decreasePostCommentCount(postId);
        if (rows != 1) {
            throw new IllegalStateException("decrease post comment count failed");
        }

        log.info(
                "delete post comment success, userUuid={}, postId={}, commentId={}",
                currUserUuid,
                postId,
                commentId
        );
    }

    @Override
    public List<CommentListItemVO> listComments(Long postId) {
        validatePostId(postId);

        String currUserUuid = UserContext.requireUuid();

        postService.checkPostInteractable(postId, currUserUuid);

        return commentMapper.selectNormalCommentsByPostId(currUserUuid, postId);
    }

    @Override
    public int increaseCommentLikeCount(Long commentId) {
        return commentMapper.increaseCommentLikeCount(commentId);
    }

    @Override
    public int decreaseCommentLikeCount(Long commentId) {
        return commentMapper.decreaseCommentLikeCount(commentId);
    }

    @Override
    public int increaseCommentReplyCount(Long commentId) {
        return commentMapper.increaseCommentReplyCount(commentId);
    }

    @Override
    public int decreaseCommentReplyCount(Long commentId) {
        return commentMapper.decreaseCommentReplyCount(commentId);
    }

    private void sendCommentEvent(Comment comment, String snippet) {
        if (comment == null) {
            return;
        }

        CommentEvent event = new CommentEvent();
        String contentSnippet = buildCommentContentSnippet(comment.getContent());
        validateContentSnippet(contentSnippet);

        event.setEventId(UUID.randomUUID().toString());
        event.setCommentId(comment.getId());
        event.setPostId(comment.getPostId());
        event.setActorUuid(comment.getUserUuid());
        event.setReceiverUuid(comment.getPostAuthorUuid());
        event.setContentSnippet(contentSnippet);
        event.setPostSnippet(snippet);
        event.setOccurredAt(now());

        commentProducer.sendCommentEvent(event);
    }

    private void validatePostId(Long postId) {
        if (postId == null || postId <= 0) {
            throw new BadRequestException("post id is invalid");
        }
    }

    private void validateCommentId(Long commentId) {
        if (commentId == null || commentId <= 0) {
            throw new BadRequestException("comment id is invalid");
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

        if (content.length() > MAX_COMMENT_CONTENT_LENGTH) {
            throw new BadRequestException("content too long");
        }
    }
}