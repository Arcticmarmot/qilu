package com.marmot.qilu.modules.comment.service.impl;

import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.event.comment.CommentEvent;
import com.marmot.qilu.common.event.comment.CommentProducer;
import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.common.exception.NotFoundException;
import com.marmot.qilu.common.util.ContentUtils;
import com.marmot.qilu.modules.comment.dto.PostCommentCreateDTO;
import com.marmot.qilu.modules.comment.entity.PostComment;
import com.marmot.qilu.modules.comment.mapper.PostCommentMapper;
import com.marmot.qilu.modules.comment.service.PostCommentService;
import com.marmot.qilu.modules.comment.vo.PostCommentListItemVO;
import com.marmot.qilu.modules.post.service.PostService;
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
public class PostCommentServiceImpl implements PostCommentService {

    private static final int STATUS_NORMAL = 1;
    private static final int MAX_COMMENT_CONTENT_LENGTH = 1024;

    private final PostCommentMapper postCommentMapper;
    private final PostService postService;
    private final CommentProducer commentProducer;

    @Override
    public void checkPostCommentInteractable(Long postId, Long commentId, String currUserUuid) {
        postService.checkPostInteractable(postId, currUserUuid);

        Integer exists = postCommentMapper.existsInteractablePostCommentById(postId, commentId);
        if (exists == null) {
            throw new NotFoundException("comment not found or not interactable");
        }
    }

    @Override
    public String getAuthorUuidById(Long commentId) {
        if (commentId == null || commentId <= 0) {
            throw new BadRequestException("comment id is invalid");
        }

        String authorUuid = postCommentMapper.selectUserUuidById(commentId);
        if (authorUuid == null) {
            throw new NotFoundException("comment not found");
        }
        return authorUuid;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPostComment(Long postId, PostCommentCreateDTO dto) {
        validatePostId(postId);

        if (dto == null) {
            throw new BadRequestException("request body must not be null");
        }

        String currUserUuid = UserContext.requireUuid();

        postService.checkPostInteractable(postId, currUserUuid);
        String postAuthorUuid = postService.getAuthorUuid(postId);

        String normalizedContent = ContentUtils.normalizeContent(dto.getContent());
        validateContent(normalizedContent);

        PostComment postComment = new PostComment();
        postComment.setPostId(postId);
        postComment.setPostAuthorUuid(postAuthorUuid);
        postComment.setUserUuid(currUserUuid);
        postComment.setContent(normalizedContent);
        postComment.setStatus(STATUS_NORMAL);

        int inserted = postCommentMapper.insert(postComment);
        if (inserted != 1) {
            throw new IllegalStateException("create comment failed");
        }

        int rows = postService.increasePostCommentCount(postId);
        if (rows != 1) {
            throw new IllegalStateException("increase post comment count failed");
        }

        sendCommentEvent(postComment);

        log.info(
                "create post comment success, userUuid={}, postId={}, commentId={}",
                currUserUuid,
                postId,
                postComment.getId()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePostComment(Long postId, Long commentId) {
        validatePostId(postId);
        validateCommentId(commentId);

        String currUserUuid = UserContext.requireUuid();

        checkPostCommentInteractable(postId, commentId, currUserUuid);

        int deleted = postCommentMapper.deletePostComment(commentId, currUserUuid);
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
    public List<PostCommentListItemVO> listPostComments(Long postId) {
        validatePostId(postId);

        String currUserUuid = UserContext.requireUuid();

        postService.checkPostInteractable(postId, currUserUuid);

        return postCommentMapper.selectNormalPostCommentsByPostId(postId);
    }

    @Override
    public int increaseCommentLikeCount(Long commentId) {
        return postCommentMapper.increaseCommentLikeCount(commentId);
    }

    @Override
    public int decreaseCommentLikeCount(Long commentId) {
        return postCommentMapper.decreaseCommentLikeCount(commentId);
    }

    @Override
    public int increaseCommentReplyCount(Long commentId) {
        return postCommentMapper.increaseCommentReplyCount(commentId);
    }

    @Override
    public int decreaseCommentReplyCount(Long commentId) {
        return postCommentMapper.decreaseCommentReplyCount(commentId);
    }

    private void sendCommentEvent(PostComment postComment) {
        if (postComment == null) {
            return;
        }

        CommentEvent event = new CommentEvent();
        String contentPreview = buildCommentContentPreview(postComment.getContent());
        validateContentPreview(contentPreview);

        event.setEventId(UUID.randomUUID().toString());
        event.setCommentId(postComment.getId());
        event.setPostId(postComment.getPostId());
        event.setActorUuid(postComment.getUserUuid());
        event.setReceiverUuid(postComment.getPostAuthorUuid());
        event.setContentPreview(contentPreview);
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

    private void validateContentPreview(String preview) {
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