package com.marmot.qilu.modules.post.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.common.exception.ForbiddenException;
import com.marmot.qilu.common.exception.NotFoundException;
import com.marmot.qilu.common.util.ContentUtils;
import com.marmot.qilu.modules.post.dto.PostCreateDTO;
import com.marmot.qilu.modules.post.dto.PostPageQueryDTO;
import com.marmot.qilu.modules.post.dto.PostUpdateDTO;
import com.marmot.qilu.modules.post.entity.Post;
import com.marmot.qilu.modules.post.mapper.PostMapper;
import com.marmot.qilu.modules.post.model.PostCreatedAtItem;
import com.marmot.qilu.modules.post.service.PostService;
import com.marmot.qilu.modules.post.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private static final int STATUS_DELETED = 0;
    private static final int STATUS_NORMAL = 1;
    private static final int MAX_POST_CONTENT_LENGTH = 4096;

    private final PostMapper postMapper;

    @Override
    public void checkPostInteractable(Long postId, String currUserUuid) {
        Integer exists = postMapper.existsInteractablePostById(postId, currUserUuid);
        if(exists == null) {
            throw new ForbiddenException("post not found or interactable");
        }
    }

    @Override
    public String getAuthorUuid(Long postId) {
        validatePostId(postId);
        String authorUuid= postMapper.selectUserUuidById(postId);
        if (authorUuid == null) {
            throw new NotFoundException("post not found");
        }
        return authorUuid;
    }

    @Override
    public List<PostPageItemVO> getPublicPostsByIds(List<Long> postIds) {
        String currUserUuid = UserContext.requireUuid();

        List<PostPageItemVO> posts =  postMapper.selectPublicPostByIds(currUserUuid, postIds);

        Map<Long, PostPageItemVO> postMap = posts.stream().collect(Collectors.toMap(PostPageItemVO::getId, item -> item));

        return postIds.stream()
                .map(postMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public Map<Long, LocalDateTime> getPublicPostCreatedAtMapByIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }

        return postMapper.selectPublicPostCreatedAtItemByIds(postIds).stream()
                .collect(Collectors.toMap(PostCreatedAtItem::getId, PostCreatedAtItem::getCreatedAt));
    }

    @Override
    public PostPreview getPostPreview(Long postId) {
        validatePostId(postId);

        PostPreview preview = postMapper.selectPostPreviewById(postId);
        if(preview == null) {
            throw new NotFoundException("post not found");
        }
        return preview;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPost(PostCreateDTO dto) {
        if(dto == null) {
            throw new BadRequestException("request body must not be null");
        }

        String currUserUuid = UserContext.requireUuid();

        String normContent = ContentUtils.normalizeContent(dto.getContent());
        String contentSnippet = ContentUtils.buildPostContentSnippet(normContent);
        validateContent(contentSnippet);

        Post post = new Post();
        post.setUserUuid(currUserUuid);
        post.setTitle(dto.getTitle());
        post.setContent(normContent);
        post.setContentSnippet(contentSnippet);
        post.setVisibility(dto.getVisibility());
        post.setStatus(STATUS_NORMAL);
        int inserted = postMapper.insert(post);
        if(inserted != 1) {
            throw new IllegalStateException("create post failed");
        }
        log.info("create post success, userUuid={}, postId={}", currUserUuid, post.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePost(Long postId, PostUpdateDTO dto) {
        validatePostId(postId);
        if(dto == null) {
            throw new BadRequestException("request body must not be null");
        }

        String currUserUuid = UserContext.requireUuid();
        checkPostInteractable(postId, currUserUuid);

        String normContent = ContentUtils.normalizeContent(dto.getContent());
        String contentSnippet = ContentUtils.buildPostContentSnippet(normContent);
        validateContent(contentSnippet);

        int updated = postMapper.update(
                null,
                new LambdaUpdateWrapper<Post>()
                        .eq(Post::getId, postId)
                        .eq(Post::getUserUuid, currUserUuid)
                        .eq(Post::getStatus, STATUS_NORMAL)
                        .set(Post::getTitle, dto.getTitle())
                        .set(Post::getContent, normContent)
                        .set(Post::getContentSnippet, contentSnippet)
                        .set(Post::getVisibility, dto.getVisibility())
        );

        if(updated != 1) {
            throw new IllegalStateException("update post failed");
        }

        log.info("update post success, userUuid={}, postId={}", currUserUuid, postId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long postId) {
        validatePostId(postId);

        String currUserUuid = UserContext.requireUuid();
        checkPostInteractable(postId, currUserUuid);

        int deleted = postMapper.update(
                null,
                new LambdaUpdateWrapper<Post>()
                        .eq(Post::getId, postId)
                        .eq(Post::getUserUuid, currUserUuid)
                        .eq(Post::getStatus, STATUS_NORMAL)
                        .set(Post::getStatus, STATUS_DELETED)
                        .set(Post::getDeletedAt, LocalDateTime.now())
        );

        if(deleted != 1) {
            throw new IllegalStateException("delete post failed");
        }

        log.info("delete post success, userUuid={}, postId={}", currUserUuid, postId);
    }

    @Override
    public PostDetailVO getMyPostDetail(Long postId) {
        String currUserUuid = UserContext.requireUuid();

        PostDetailVO vo = postMapper.selectMyPostDetail(postId, currUserUuid);
        if(vo == null) {
            throw new NotFoundException("post not found");
        }
        return vo;
    }

    @Override
    public PostPageVO<PostPageItemVO> getMyPostPage(PostPageQueryDTO dto) {
        String currUserUuid = UserContext.requireUuid();

        long current = dto.getCurrent();
        long size = dto.getSize();
        long offset = (current - 1) * size;
        Long total = postMapper.countMyPosts(currUserUuid);

        List<PostPageItemVO> records = postMapper.selectMyPostPage(offset, size, currUserUuid);
        PostPageVO<PostPageItemVO> pageVO = new PostPageVO<>();
        pageVO.setCurrent(current);
        pageVO.setSize(size);
        pageVO.setTotal(total);
        pageVO.setRecords(records);
        return pageVO;
    }

    @Override
    public PostDetailVO getPublicPostDetail(Long postId) {
        String currUserUuid = UserContext.requireUuid();

        PostDetailVO vo = postMapper.selectPublicPostDetail(postId, currUserUuid);
        if(vo == null) {
            throw new NotFoundException("post not found");
        }
        return vo;
    }

    @Override
    public PostPageVO<PostPageItemVO> getPublicPostPage(PostPageQueryDTO dto) {
        String currUserUuid = UserContext.requireUuid();

        long current = dto.getCurrent();
        long size = dto.getSize();
        long offset = (current - 1) * size;
        Long total = postMapper.countPublicPosts();

        List<PostPageItemVO> records = postMapper.selectPublicPostPage(offset, size, currUserUuid);
        PostPageVO<PostPageItemVO> pageVO = new PostPageVO<>();
        pageVO.setCurrent(current);
        pageVO.setSize(size);
        pageVO.setTotal(total);
        pageVO.setRecords(records);
        return pageVO;
    }

    @Override
    public int increasePostLikeCount(Long postId) {
        return postMapper.increasePostLikeCount(postId);
    }

    @Override
    public int decreasePostLikeCount(Long postId) {
        return postMapper.decreasePostLikeCount(postId);
    }

    @Override
    public int increasePostCommentCount(Long postId) {
        return postMapper.increasePostCommentCount(postId);
    }

    @Override
    public int decreasePostCommentCount(Long postId) {
        return postMapper.decreasePostCommentCount(postId);
    }

    private void validatePostId(Long postId) {
        if(postId == null || postId <= 0) {
            throw new BadRequestException("post id must not be blank");
        }
    }

    private void validateContent(String content) {
        if (content == null || content.isEmpty()) {
            throw new BadRequestException("content must not be blank");
        }
        if (content.length() > MAX_POST_CONTENT_LENGTH) {
            throw new BadRequestException("content too long");
        }
    }
}
