package com.marmot.qilu.modules.comment.service.impl;

import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.event.comment.CommentEntityType;
import com.marmot.qilu.common.event.comment.CommentEvent;
import com.marmot.qilu.common.event.comment.CommentProducer;
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

import static java.time.LocalDateTime.now;

@Service
@RequiredArgsConstructor
public class PostCommentServiceImpl implements PostCommentService {

    private static final int STATUS_NORMAL = 1;
    private static final int STATUS_DELETED = 0;

    private static final int PREVIEW_LENGTH = 10;

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

        PostComment postComment = new PostComment();
        postComment.setPostId(postId);
        postComment.setPostAuthorUuid(postAuthorUuid);
        postComment.setUserUuid(currUserUuid);
        postComment.setContent(dto.getContent());
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
    public void deletePostComment(Long commentId) {
        if(commentId == null || commentId <= 0) {
            throw new RuntimeException("CommentId is invalid.");
        }

        String currUserUuid = UserContext.requireUuid();

        PostComment postComment = postCommentMapper.selectById(commentId);

        if (postComment == null
                || postComment.getStatus() == null
                || postComment.getStatus() == STATUS_DELETED) {
            throw new RuntimeException("Comment does not exist.");
        }

        if (!currUserUuid.equals(postComment.getUserUuid())) {
            throw new RuntimeException("No permission to delete this comment.");
        }

        int deleted = postCommentMapper.deletePostComment(commentId, currUserUuid);

        if(deleted != 1) {
            throw new RuntimeException("Failed to delete comment.");
        }
        int rows = postService.decreasePostCommentCount(postComment.getPostId());
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
        event.setEventId(UUID.randomUUID().toString());
        event.setCommentId(postComment.getId());
        event.setEntityId(postComment.getPostId());
        event.setEntityType(CommentEntityType.POST);
        event.setActorUuid(postComment.getUserUuid());
        event.setReceiverUuid(postComment.getPostAuthorUuid());
        event.setContentPreview(buildContentPreview(postComment.getContent()));
        event.setOccurredAt(now());

        commentProducer.sendCommentEvent(event);
    }

    private String buildContentPreview(String content) {
        if (content == null) {
            return "";
        }

        String normalized = content
                .replace("\r", " ")
                .replace("\n", " ")
                .trim();

        if (normalized.isEmpty()) {
            return "";
        }

        return normalized.substring(0, Math.min(normalized.length(), PREVIEW_LENGTH));
    }
}
