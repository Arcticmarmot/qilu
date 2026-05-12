package com.marmot.qilu.modules.post.service.impl;

import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.common.exception.ForbiddenException;
import com.marmot.qilu.common.exception.NotFoundException;
import com.marmot.qilu.modules.post.dto.*;
import com.marmot.qilu.modules.post.mapper.PostMapper;
import com.marmot.qilu.modules.post.mapper.PostMediaMapper;
import com.marmot.qilu.modules.post.model.PostCreatedAtItem;
import com.marmot.qilu.modules.post.model.PostPreview;
import com.marmot.qilu.modules.post.model.PostSearchSource;
import com.marmot.qilu.modules.post.service.PostQueryService;
import com.marmot.qilu.modules.post.vo.PostDetailVO;
import com.marmot.qilu.modules.post.vo.PostMediaVO;
import com.marmot.qilu.modules.post.vo.PostPageItemVO;
import com.marmot.qilu.modules.post.vo.PostPageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostQueryServiceImpl implements PostQueryService {

    private final PostMapper postMapper;
    private final PostMediaMapper postMediaMapper;

    @Override
    public void checkPostInteractable(Long postId, String currUserUuid) {
        Integer exists = postMapper.existsInteractablePostById(postId, currUserUuid);
        if(exists == null) {
            throw new ForbiddenException("post not found or interactable");
        }
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

    private void validatePostId(Long postId) {
        if(postId == null || postId <= 0) {
            throw new BadRequestException("post id must not be blank");
        }
    }
}
