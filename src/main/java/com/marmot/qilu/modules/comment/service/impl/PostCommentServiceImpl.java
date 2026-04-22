package com.marmot.qilu.modules.comment.service.impl;

import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.event.comment.CommentEntityType;
import com.marmot.qilu.common.event.comment.CommentEvent;
import com.marmot.qilu.common.event.comment.CommentProducer;
import com.marmot.qilu.common.util.ContentUtils;
import com.marmot.qilu.modules.comment.dto.PostCommentCreateDTO;
import com.marmot.qilu.modules.comment.entity.PostComment;
import com.marmot.qilu.modules.comment.mapper.PostCommentMapper;
import com.marmot.qilu.modules.comment.service.PostCommentService;
import com.marmot.qilu.modules.comment.vo.PostCommentListItemVO;
import com.marmot.qilu.modules.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.marmot.qilu.common.util.ContentUtils.buildCommentContentPreview;
import static java.time.LocalDateTime.now;

@Service
@RequiredArgsConstructor
public class PostCommentServiceImpl implements PostCommentService {

    private static final int STATUS_NORMAL = 1;
    private static final int STATUS_DELETED = 0;

    private static final int MAX_COMMENT_CONTENT_LENGTH = 1024;

    private final PostCommentMapper postCommentMapper;
    private final PostService postService;
    private final CommentProducer commentProducer;

    @Override
    public void checkPostCommentInteractable(Long postId, Long commentId, String currUserUuid) {
        postService.checkPostInteractable(postId, currUserUuid);
        Integer exists = postCommentMapper.existsInteractablePostCommentById(postId, commentId);
        if(exists == null) {
            throw new RuntimeException("Comment not interactable");
        }
    }

    @Override
    public String getAuthorUuidById(Long commentId) {
        String authorUuid = postCommentMapper.selectUserUuidById(commentId);
        if (authorUuid == null) {
            throw new RuntimeException("Comment not found");
        }
        return authorUuid;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPostComment(Long postId, PostCommentCreateDTO dto) {
        if(postId == null || postId <= 0) {
            throw new RuntimeException("PostId is invalid.");
        }
        if(dto == null) {
            throw new RuntimeException("Request body must not be null.");
        }

        String currUserUuid = UserContext.requireUuid();

        postService.checkPostInteractable(postId, currUserUuid);
        String postAuthorUuid = postService.getPostAuthorUuid(postId);
        String normalizedContent = ContentUtils.normalizeContent(dto.getContent());
        validateContent(normalizedContent);
        PostComment postComment = new PostComment();
        postComment.setPostId(postId);
        postComment.setPostAuthorUuid(postAuthorUuid);
        postComment.setUserUuid(currUserUuid);
        postComment.setContent(normalizedContent);
        postComment.setStatus(STATUS_NORMAL);

        int inserted = postCommentMapper.insert(postComment);
        if(inserted != 1) {
            throw new RuntimeException("Failed to create comment.");
        }
        int rows = postService.increasePostCommentCount(postId);
        if(rows != 1) {
            throw new RuntimeException("Failed to create comment.");
        }

        sendCommentEvent(postComment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePostComment(Long postId, Long commentId) {
        if(postId == null || postId <= 0) {
            throw new RuntimeException("PostId is invalid.");
        }

        if(commentId == null || commentId <= 0) {
            throw new RuntimeException("CommentId is invalid.");
        }

        String currUserUuid = UserContext.requireUuid();

        checkPostCommentInteractable(postId, commentId, currUserUuid);

        int deleted = postCommentMapper.deletePostComment(commentId, currUserUuid);

        if(deleted != 1) {
            throw new RuntimeException("Comment not found or no permission to delete.");
        }
        int rows = postService.decreasePostCommentCount(postId);
        if(rows != 1) {
            throw new RuntimeException("Failed to delete comment.");
        }
    }

    @Override
    public List<PostCommentListItemVO> listPostComments(Long postId) {
        if (postId == null || postId <= 0) {
            throw new RuntimeException("PostId is invalid.");
        }

        String currUserUuid = UserContext.requireUuid();

        postService.checkPostInteractable(postId, currUserUuid);

        return postCommentMapper.selectNormalPostCommentsByPostId(postId);
    }

    private void sendCommentEvent(PostComment postComment) {
        if(postComment == null) {
            return;
        }

        CommentEvent event = new CommentEvent();
        String contentPreview = buildCommentContentPreview(postComment.getContent());
        validateContentPreview(contentPreview);
        event.setEventId(UUID.randomUUID().toString());
        event.setCommentId(postComment.getId());
        event.setEntityId(postComment.getPostId());
        event.setEntityType(CommentEntityType.POST);
        event.setActorUuid(postComment.getUserUuid());
        event.setReceiverUuid(postComment.getPostAuthorUuid());
        event.setContentPreview(contentPreview);
        event.setOccurredAt(now());

        commentProducer.sendCommentEvent(event);
    }

    private void validateContentPreview(String preview) {
        if(preview == null || preview.isEmpty()) {
            throw new RuntimeException("Preview cannot be blank.");
        }
    }

    private void validateContent(String content) {
        if(content == null || content.isEmpty()) {
            throw new RuntimeException("Preview cannot be blank.");
        }

        if(content.length() > MAX_COMMENT_CONTENT_LENGTH) {
            throw new RuntimeException("Content too long.");
        }
    }
}
