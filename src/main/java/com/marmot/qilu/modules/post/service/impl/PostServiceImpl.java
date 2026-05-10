package com.marmot.qilu.modules.post.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.common.exception.ForbiddenException;
import com.marmot.qilu.common.exception.NotFoundException;
import com.marmot.qilu.common.util.ContentUtils;
import com.marmot.qilu.modules.media.service.MediaFileService;
import com.marmot.qilu.modules.post.dto.PostBranchCreateDTO;
import com.marmot.qilu.modules.post.dto.PostCreateDTO;
import com.marmot.qilu.modules.post.dto.PostPageQueryDTO;
import com.marmot.qilu.modules.post.dto.PostUpdateDTO;
import com.marmot.qilu.modules.post.entity.Post;
import com.marmot.qilu.modules.post.entity.PostMedia;
import com.marmot.qilu.modules.post.mapper.PostMapper;
import com.marmot.qilu.modules.post.mapper.PostMediaMapper;
import com.marmot.qilu.modules.post.model.PostCreatedAtItem;
import com.marmot.qilu.modules.post.model.PostPreview;
import com.marmot.qilu.modules.post.model.PostTreeInfo;
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
    private static final int MAX_BRANCH_PROMPT_LENGTH = 128;

    private final PostMapper postMapper;
    private final PostMediaMapper postMediaMapper;
    private final MediaFileService mediaFileService;

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

        List<PostPageItemVO> records =  postMapper.selectPublicPostByIds(currUserUuid, postIds);
        fillPostPageCoverUrl(records);

        Map<Long, PostPageItemVO> postMap = records.stream().collect(Collectors.toMap(PostPageItemVO::getId, item -> item));

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

        List<Long> mediaIds = dto.getMediaIds();
        validateMediaIds(mediaIds);

        Post post = new Post();
        post.setUserUuid(currUserUuid);
        post.setTitle(dto.getTitle());
        post.setContent(normContent);
        post.setContentSnippet(contentSnippet);
        post.setVisibility(dto.getVisibility());
        post.setStatus(STATUS_NORMAL);
        post.setParentId(null);
        post.setRootId(null);
        post.setBranchPrompt(null);

        int inserted = postMapper.insert(post);
        if(inserted != 1) {
            throw new IllegalStateException("create post failed");
        }

        Long postId = post.getId();
        if(postId == null) {
            throw new IllegalStateException("post id is not generated");
        }

        int updatedRootId = postMapper.update(
                null,
                new LambdaUpdateWrapper<Post>()
                        .eq(Post::getId, postId)
                        .set(Post::getRootId, postId)
        );

        if(updatedRootId != 1) {
            throw new IllegalStateException("update post root id failed");
        }

        if(mediaIds != null && !mediaIds.isEmpty()) {
            bindPostMedia(post.getId(), mediaIds);

            int updated = mediaFileService.markMediaFilesUsed(mediaIds);
            if(updated != mediaIds.size()) {
                throw new BadRequestException("media ids are invalid");
            }
        }
        log.info("create post success, userUuid={}, postId={}", currUserUuid, post.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createBranchPost(Long parentPostId, PostBranchCreateDTO dto) {
        validatePostId(parentPostId);

        if(dto == null) {
            throw new BadRequestException("request body must not be null");
        }

        String currUserUuid = UserContext.requireUuid();
        String normBranchPrompt = ContentUtils.normalizeContent(dto.getBranchPrompt());
        validateBranchPrompt(normBranchPrompt);
        String normContent = ContentUtils.normalizeContent(dto.getContent());
        String contentSnippet = ContentUtils.buildPostContentSnippet(normContent);
        validateContent(contentSnippet);

        List<Long> mediaIds = dto.getMediaIds();
        validateMediaIds(mediaIds);

        PostTreeInfo parentInfo = postMapper.selectPostTreeInfo(parentPostId);
        if(parentInfo == null) {
            throw new NotFoundException("parent post not found");
        }

        Long rootId = parentInfo.getRootId();
        if(rootId == null) {
            rootId = parentInfo.getId();
        }

        Post post = new Post();
        post.setUserUuid(currUserUuid);
        post.setTitle(dto.getTitle());
        post.setContent(normContent);
        post.setContentSnippet(contentSnippet);
        post.setVisibility(dto.getVisibility());
        post.setStatus(STATUS_NORMAL);

        // branch post
        post.setParentId(parentInfo.getId());
        post.setRootId(rootId);
        post.setBranchPrompt(normBranchPrompt);

        int inserted = postMapper.insert(post);
        if(inserted != 1) {
            throw new IllegalStateException("create post branch failed");
        }

        if(mediaIds != null && !mediaIds.isEmpty()) {
            bindPostMedia(post.getId(), mediaIds);

            int updated = mediaFileService.markMediaFilesUsed(mediaIds);
            if(updated != mediaIds.size()) {
                throw new BadRequestException("media ids are invalid");
            }
        }

        log.info("create post branch success, userUuid={}, parentPostId={}, postId={}",
                currUserUuid, parentPostId, post.getId());    }

    private void bindPostMedia(Long postId, List<Long> mediaIds) {
        for(int i = 0; i < mediaIds.size(); i++) {
            PostMedia postMedia = new PostMedia();
            postMedia.setPostId(postId);
            postMedia.setMediaId(mediaIds.get(i));
            postMedia.setSortOrder(i);

            int inserted = postMediaMapper.insert(postMedia);
            if(inserted != 1) {
                throw new IllegalStateException("create post media failed");
            }
        }
    }

    private void validateMediaIds(List<Long> mediaIds) {
        if (mediaIds == null || mediaIds.isEmpty()) {
            return;
        }

        if (mediaIds.size() > 10) {
            throw new BadRequestException("media count must not exceed 10");
        }

        for (Long mediaId : mediaIds) {
            if (mediaId == null || mediaId <= 0) {
                throw new BadRequestException("media ids are invalid");
            }
        }
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
    public List<PostDetailVO> getMyPostDetail(Long postId) {
        String currUserUuid = UserContext.requireUuid();

        List<PostDetailVO> records = postMapper.selectMyPostDetail(postId, currUserUuid);
        if(records == null || records.isEmpty()) {
            throw new NotFoundException("post not found");
        }

        fillPostDetailMediaList(records);
        return records;
    }

    @Override
    public PostPageVO<PostPageItemVO> getMyPostPage(PostPageQueryDTO dto) {
        String currUserUuid = UserContext.requireUuid();

        long current = dto.getCurrent();
        long size = dto.getSize();
        long offset = (current - 1) * size;
        Long total = postMapper.countMyPosts(currUserUuid);

        List<PostPageItemVO> records = postMapper.selectMyPostPage(offset, size, currUserUuid);
        fillPostPageCoverUrl(records);
        PostPageVO<PostPageItemVO> pageVO = new PostPageVO<>();
        pageVO.setCurrent(current);
        pageVO.setSize(size);
        pageVO.setTotal(total);
        pageVO.setRecords(records);
        return pageVO;
    }

    @Override
    public List<PostDetailVO> getPublicPostDetail(Long postId) {
        String currUserUuid = UserContext.requireUuid();

        List<PostDetailVO> records = postMapper.selectPublicPostDetail(postId, currUserUuid);
        if(records == null || records.isEmpty()) {
            throw new NotFoundException("post not found");
        }
        fillPostDetailMediaList(records);
        return records;
    }

    @Override
    public PostPageVO<PostPageItemVO> getPublicPostPage(PostPageQueryDTO dto) {
        String currUserUuid = UserContext.requireUuid();

        long current = dto.getCurrent();
        long size = dto.getSize();
        long offset = (current - 1) * size;
        Long total = postMapper.countPublicPosts();

        List<PostPageItemVO> records = postMapper.selectPublicPostPage(offset, size, currUserUuid);
        fillPostPageCoverUrl(records);
        PostPageVO<PostPageItemVO> pageVO = new PostPageVO<>();
        pageVO.setCurrent(current);
        pageVO.setSize(size);
        pageVO.setTotal(total);
        pageVO.setRecords(records);
        return pageVO;
    }

    private void fillPostPageCoverUrl(List<PostPageItemVO> records) {
        if(records == null || records.isEmpty()) {
            return;
        }

        List<Long> postIds = records.stream()
                .map(PostPageItemVO::getId)
                .filter(Objects::nonNull)
                .toList();

        if(postIds.isEmpty()) {
            return;
        }

        List<PostMediaVO> coverList = postMediaMapper.selectCoverMediaByPostIds(postIds);

        Map<Long, String> coverUrlMap = coverList.stream().collect(
                Collectors.toMap(PostMediaVO::getPostId, PostMediaVO::getUrl,
                        (oldValue, newValue) -> oldValue)
        );

        for(PostPageItemVO record: records) {
            record.setCoverUrl(coverUrlMap.get(record.getId()));
        }
    }

    private void fillPostDetailMediaList(PostDetailVO vo) {
        if(vo == null) {
            return;
        }

        List<PostMediaVO> mediaList = postMediaMapper.selectPostMediaListById(vo.getId());
        vo.setMediaList(mediaList);
    }

    private void fillPostDetailMediaList(List<PostDetailVO> records) {
        if(records == null || records.isEmpty()) {
            return;
        }
        for(PostDetailVO record: records) {
            fillPostDetailMediaList(record);
        }
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

    private void validateBranchPrompt(String branchPrompt) {
        if(branchPrompt == null || branchPrompt.isEmpty()) {
            throw new BadRequestException("branch prompt must not be blank");
        }

        if(branchPrompt.length() > MAX_BRANCH_PROMPT_LENGTH) {
            throw new BadRequestException("branch prompt too long");
        }
    }
}
