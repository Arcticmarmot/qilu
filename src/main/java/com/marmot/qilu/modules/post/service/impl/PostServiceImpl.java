package com.marmot.qilu.modules.post.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.event.search.PostSearchIndexAction;
import com.marmot.qilu.common.event.search.PostSearchIndexEvent;
import com.marmot.qilu.common.event.search.PostSearchIndexProducer;
import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.common.exception.ForbiddenException;
import com.marmot.qilu.common.exception.NotFoundException;
import com.marmot.qilu.common.util.ContentUtils;
import com.marmot.qilu.modules.media.service.MediaFileService;
import com.marmot.qilu.modules.post.dto.*;
import com.marmot.qilu.modules.post.entity.Post;
import com.marmot.qilu.modules.post.entity.PostMedia;
import com.marmot.qilu.modules.post.mapper.PostMapper;
import com.marmot.qilu.modules.post.mapper.PostMediaMapper;
import com.marmot.qilu.modules.post.model.PostCreatedAtItem;
import com.marmot.qilu.modules.post.model.PostPreview;
import com.marmot.qilu.modules.post.model.PostSearchSource;
import com.marmot.qilu.modules.post.model.PostTreeInfo;
import com.marmot.qilu.modules.post.service.PostService;
import com.marmot.qilu.modules.post.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
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
    private final PostSearchIndexProducer postSearchIndexProducer;

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

        Integer visibility = dto.getVisibility();
        validateVisibility(visibility);

        Post post = new Post();
        post.setUserUuid(currUserUuid);
        post.setTitle(dto.getTitle());
        post.setContent(normContent);
        post.setContentSnippet(contentSnippet);
        post.setVisibility(visibility);
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

        sendPostSearchIndexEventAfterCommit(List.of(postId), postId, PostSearchIndexAction.SYNC, currUserUuid);
        log.info("create post success, userUuid={}, postId={}", currUserUuid, post.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createBranchPost(Long parentPostId, BranchPostCreateDTO dto) {
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

        PostTreeInfo parentTreeInfo = postMapper.selectInteractablePostTreeInfo(currUserUuid, parentPostId);
        if(parentTreeInfo == null) {
            throw new NotFoundException("parent post not found or no permission");
        }

        Post post = new Post();
        post.setUserUuid(currUserUuid);
        post.setTitle(dto.getTitle());
        post.setContent(normContent);
        post.setContentSnippet(contentSnippet);
        post.setBranchPrompt(normBranchPrompt);
        post.setVisibility(parentTreeInfo.getVisibility());
        post.setStatus(parentTreeInfo.getStatus());
        post.setParentId(parentTreeInfo.getId());
        post.setRootId(parentTreeInfo.getRootId());

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
        Long postId = post.getId();
        Long rootId = post.getRootId();
        sendPostSearchIndexEventAfterCommit(List.of(postId), rootId, PostSearchIndexAction.SYNC, currUserUuid);

        log.info("create post branch success, userUuid={}, parentPostId={}, postId={}",
                currUserUuid, parentPostId, post.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePost(Long postId, PostUpdateDTO dto) {
        validatePostId(postId);
        if(dto == null) {
            throw new BadRequestException("request body must not be null");
        }

        String currUserUuid = UserContext.requireUuid();

        String normContent = ContentUtils.normalizeContent(dto.getContent());
        String contentSnippet = ContentUtils.buildPostContentSnippet(normContent);
        validateContent(contentSnippet);

        PostTreeInfo currTreeInfo = postMapper.selectInteractablePostTreeInfo(currUserUuid, postId);
        if(currTreeInfo == null) {
            throw new NotFoundException("post not found or no permission");
        }

        Long parentId = currTreeInfo.getParentId();
        Long rootId= currTreeInfo.getRootId();
        if(parentId == null) {
            Integer visibility = dto.getVisibility();
            validateVisibility(visibility);
            List<Long> subtreePostIds = postMapper.selectSubtreePostIdsById(postId);
            if (subtreePostIds == null || subtreePostIds.isEmpty()) {
                throw new IllegalStateException("query post subtree failed");
            }
            int updated = postMapper.update(
                    null,
                    new LambdaUpdateWrapper<Post>()
                            .eq(Post::getId, postId)
                            .eq(Post::getUserUuid, currUserUuid)
                            .eq(Post::getStatus, STATUS_NORMAL)
                            .eq(Post::getRootId, postId)
                            .isNull(Post::getParentId)
                            .isNull(Post::getBranchPrompt)
                            .set(Post::getTitle, dto.getTitle())
                            .set(Post::getContent, normContent)
                            .set(Post::getContentSnippet, contentSnippet)
            );
            if(updated != 1) {
                throw new IllegalStateException("update post failed");
            }
            int updatedVisibility = postMapper.updateVisibilityByIds(currUserUuid, subtreePostIds, visibility);
            if(updatedVisibility != subtreePostIds.size()) {
                throw new IllegalStateException("update post tree visibility failed");
            }

            sendPostSearchIndexEventAfterCommit(subtreePostIds, rootId,
                    PostSearchIndexAction.SYNC, currUserUuid);
        } else {
            String branchPrompt = ContentUtils.normalizeContent(dto.getBranchPrompt());
            validateBranchPrompt(branchPrompt);
            int updated = postMapper.update(
                    null,
                    new LambdaUpdateWrapper<Post>()
                            .eq(Post::getId, postId)
                            .eq(Post::getUserUuid, currUserUuid)
                            .eq(Post::getStatus, STATUS_NORMAL)
                            .isNotNull(Post::getParentId)
                            .isNotNull(Post::getBranchPrompt)
                            .isNotNull(Post::getRootId)
                            .set(Post::getBranchPrompt, branchPrompt)
                            .set(Post::getTitle, dto.getTitle())
                            .set(Post::getContent, normContent)
                            .set(Post::getContentSnippet, contentSnippet)
            );
            if(updated != 1) {
                throw new IllegalStateException("update post failed");
            }
            sendPostSearchIndexEventAfterCommit(List.of(postId), rootId,
                    PostSearchIndexAction.SYNC, currUserUuid);
        }

        log.info("update post success, userUuid={}, postId={}", currUserUuid, postId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePostParent(Long postId, PostParentUpdateDTO dto) {
        validatePostId(postId);

        if(dto == null) {
            throw new BadRequestException("request body must not be null");
        }

        String currUserUuid = UserContext.requireUuid();

        PostTreeInfo currTreeInfo = postMapper.selectInteractablePostTreeInfo(currUserUuid, postId);

        if(currTreeInfo == null) {
            throw new NotFoundException("post not found or no permission");
        }

        Long parentId = dto.getParentId();
        if(parentId == null) {
            updatePostAsRoot(postId, currUserUuid, currTreeInfo);
            return;
        }

        updatePostAsBranch(postId, parentId, currUserUuid, currTreeInfo, dto);
    }

    private void updatePostAsRoot(Long postId, String currUserUuid, PostTreeInfo currTreeInfo) {
        if(currTreeInfo.getParentId() == null && Objects.equals(currTreeInfo.getRootId(), currTreeInfo.getId())) {
            throw new BadRequestException("post is already a root post");
        }

        List<Long> subtreePostIds = postMapper.selectSubtreePostIdsById(postId);
        if(subtreePostIds == null || subtreePostIds.isEmpty()) {
            throw new IllegalStateException("query post subtree failed");
        }

        int updated = postMapper.update(
                null,
                new LambdaUpdateWrapper<Post>()
                        .eq(Post::getId, postId)
                        .eq(Post::getUserUuid, currUserUuid)
                        .eq(Post::getStatus, STATUS_NORMAL)
                        .set(Post::getParentId, null)
                        .set(Post::getBranchPrompt, null)
        );

        if(updated != 1) {
            throw new NotFoundException("post not found or no permission");
        }

        int updatedRoot = postMapper.updateRootIdByIds(currUserUuid, subtreePostIds, postId);
        if(updatedRoot != subtreePostIds.size()) {
            throw new IllegalStateException("update post subtree root id failed");
        }

        sendPostSearchIndexEventAfterCommit(subtreePostIds, postId,
                PostSearchIndexAction.SYNC, currUserUuid);

        log.info("update post tree as root success, userUuid={}, postId={}, parentPostId={}",
                currUserUuid, postId, null);
    }

    private void updatePostAsBranch(Long postId, Long parentId, String currUserUuid,
                                    PostTreeInfo currTreeInfo, PostParentUpdateDTO dto) {
        validatePostId(parentId);

        if(Objects.equals(postId, parentId)) {
            throw new BadRequestException("post cannot be moved to itself");
        }

        PostTreeInfo parentTreeInfo = postMapper.selectInteractablePostTreeInfo(currUserUuid, parentId);
        if(parentTreeInfo == null) {
            throw new NotFoundException("parent post not found");
        }

        List<Long> subtreePostIds = postMapper.selectSubtreePostIdsById(postId);
        if(subtreePostIds == null || subtreePostIds.isEmpty()) {
            throw new IllegalStateException("query post subtree failed");
        }

        if(subtreePostIds.contains(parentId)) {
            throw new BadRequestException("post cannot be moved to its descendant");
        }

        String normBranchPrompt = ContentUtils.normalizeContent(dto.getBranchPrompt());
        validateBranchPrompt(normBranchPrompt);

        Long newRootId = parentTreeInfo.getRootId();
        if(newRootId == null) {
            throw new IllegalStateException("root id cannot be null");
        }

        int updated = postMapper.update(
                null,
                new LambdaUpdateWrapper<Post>()
                        .eq(Post::getId, postId)
                        .eq(Post::getUserUuid, currUserUuid)
                        .eq(Post::getStatus, STATUS_NORMAL)
                        .set(Post::getParentId, parentId)
                        .set(Post::getBranchPrompt, normBranchPrompt)
        );

        if(updated != 1) {
            throw new NotFoundException("post not found or no permission");
        }

        Long oldRootId = currTreeInfo.getRootId();
        if(oldRootId == null) {
            throw new IllegalStateException("root id cannot be null");
        }

        if(!Objects.equals(oldRootId, newRootId)) {
            int updatedRoot = postMapper.updateRootIdByIds(currUserUuid, subtreePostIds, newRootId);
            if(updatedRoot != subtreePostIds.size()) {
                throw new IllegalStateException("update post subtree root id failed");
            }
            sendPostSearchIndexEventAfterCommit(subtreePostIds, newRootId, PostSearchIndexAction.SYNC, currUserUuid);
        } else {
            sendPostSearchIndexEventAfterCommit(List.of(postId), newRootId, PostSearchIndexAction.SYNC, currUserUuid);
        }

        log.info("update post tree as branch success, userUuid={}, postId={}, parentPostId={}",
                currUserUuid, postId, parentId);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long postId) {
        validatePostId(postId);

        String currUserUuid = UserContext.requireUuid();

        PostTreeInfo currTreeInfo = postMapper.selectInteractablePostTreeInfo(currUserUuid, postId);
        if(currTreeInfo == null) {
            throw new NotFoundException("post not found or no permission");
        }

        Long parentId = currTreeInfo.getParentId();
        Long rootId = currTreeInfo.getRootId();
        List<Long> subtreePostIds;
        if(parentId == null) {
            subtreePostIds = postMapper.selectSubtreePostIdsByRootId(postId);
        } else {
            subtreePostIds = postMapper.selectSubtreePostIdsById(postId);
        }

        if(subtreePostIds == null || subtreePostIds.isEmpty()) {
            throw new IllegalStateException("query post subtree failed");
        }

        int deletedStatus = postMapper.updateStatusByIds(currUserUuid, subtreePostIds, STATUS_DELETED);
        if(deletedStatus != subtreePostIds.size()) {
            throw new IllegalStateException("delete post failed");
        }

        sendPostSearchIndexEventAfterCommit(subtreePostIds, rootId,
                PostSearchIndexAction.DELETE, currUserUuid);

        log.info("delete post tree success, userUuid={}, postId={}", currUserUuid, postId);
    }

    @Override
    public void checkPostInteractable(Long postId, String currUserUuid) {
        Integer exists = postMapper.existsInteractablePostById(postId, currUserUuid);
        if(exists == null) {
            throw new ForbiddenException("post not found or interactable");
        }
    }

    @Override
    public void checkBranchPostNormal(Long postId) {
        Integer exists = postMapper.existsNormalBranchPostById(postId);
        if(exists == null) {
            throw new ForbiddenException("post is not a branch post");
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
    public List<PostSearchSource> getPostSearchSourceList(List<Long> postIds) {
        validatePostIds(postIds);

        List<PostSearchSource> sourceList = postMapper.selectPostSearchSourceListByIds(postIds);
        if(sourceList == null || sourceList.isEmpty()) {
            return List.of();
        }
        return sourceList;
    }

    private void validatePostIds(List<Long> postIds) {
        for(Long postId: postIds) {
            validatePostId(postId);
        }
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
        return new PostPageVO<>(current, size, total, records);
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
        return new PostPageVO<>(current, size, total, records);
    }

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

    private void fillPostDetailMediaList(List<PostDetailVO> records) {
        if(records == null || records.isEmpty()) {
            return;
        }
        for(PostDetailVO record: records) {
            List<PostMediaVO> mediaList = postMediaMapper.selectPostMediaListById(record.getId());
            record.setMediaList(mediaList);
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

    private void sendPostSearchIndexEventAfterCommit(List<Long> postIds,
                                                     Long rootId,
                                                     PostSearchIndexAction action,
                                                     String currUserUuid) {
        PostSearchIndexEvent event = new PostSearchIndexEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setPostIds(postIds);
        event.setRootId(rootId);
        event.setAction(action);
        event.setOperatorUuid(currUserUuid);
        event.setOccurredAt(LocalDateTime.now());

        runAfterCommit(() -> postSearchIndexProducer.sendPostSearchIndexEvent(event));
    }

    private void runAfterCommit(Runnable task) {
        if(TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
            return;
        }
        task.run();
    }

    private void validatePostId(Long postId) {
        if(postId == null || postId <= 0) {
            throw new BadRequestException("post id must not be blank");
        }
    }


    private void validateVisibility(Integer visibility) {
        if(visibility == null) {
            throw new BadRequestException("visibility must not be blank");
        }

        if(visibility != 1 && visibility != 2) {
            throw new BadRequestException("visibility can only be 1 or 2");
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

    private void validateBranchPrompt(String branchPrompt) {
        if(branchPrompt == null || branchPrompt.isEmpty()) {
            throw new BadRequestException("branch prompt must not be blank");
        }

        if(branchPrompt.length() > MAX_BRANCH_PROMPT_LENGTH) {
            throw new BadRequestException("branch prompt too long");
        }
    }
}
