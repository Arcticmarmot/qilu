package com.marmot.qilu.modules.post.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.util.ContentUtils;
import com.marmot.qilu.modules.post.dto.PostCreateDTO;
import com.marmot.qilu.modules.post.dto.PostPageQueryDTO;
import com.marmot.qilu.modules.post.dto.PostUpdateDTO;
import com.marmot.qilu.modules.post.entity.Post;
import com.marmot.qilu.modules.post.mapper.PostMapper;
import com.marmot.qilu.modules.post.service.PostService;
import com.marmot.qilu.modules.post.vo.PostDetailVO;
import com.marmot.qilu.modules.post.vo.PostPageItemVO;
import com.marmot.qilu.modules.post.vo.PostPageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
            throw new RuntimeException("Post not interactable");
        }
    }

    @Override
    public String getPostAuthorUuid(Long postId) {
        String authorUuid= postMapper.selectUserUuidById(postId);
        if (authorUuid == null) {
            throw new RuntimeException("Post not found");
        }
        return authorUuid;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createPost(PostCreateDTO dto) {
        String currUserUuid = UserContext.requireUuid();

        if(dto == null) {
            throw new RuntimeException("Request body must not be null.");
        }
        String normalizedContent = ContentUtils.normalizeContent(dto.getContent());
        validateContent(normalizedContent);

        Post post = new Post();
        post.setUserUuid(currUserUuid);
        post.setTitle(dto.getTitle());
        post.setContent(normalizedContent);
        post.setVisibility(dto.getVisibility());
        post.setStatus(STATUS_NORMAL);
        int inserted = postMapper.insert(post);
        if(inserted != 1) {
            throw new RuntimeException("Create post failed.");
        }
    }

    @Override
    public PostDetailVO getMyPostDetail(Long postId) {
        String currUserUuid = UserContext.requireUuid();

        PostDetailVO vo = postMapper.selectMyPostDetail(postId, currUserUuid);
        if(vo == null) {
            throw new RuntimeException("Post not found");
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
            throw new RuntimeException("Post not found");
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
    public void updatePost(Long postId, PostUpdateDTO dto) {
        String currUserUuid = UserContext.requireUuid();

        int updated = postMapper.update(
                null,
                new LambdaUpdateWrapper<Post>()
                        .eq(Post::getId, postId)
                        .eq(Post::getUserUuid, currUserUuid)
                        .eq(Post::getStatus, STATUS_NORMAL)
                        .set(Post::getTitle, dto.getTitle())
                        .set(Post::getContent, dto.getContent())
                        .set(Post::getVisibility, dto.getVisibility())
        );
        if(updated == 0) {
            throw new RuntimeException("posts not found or no permissions.");
        }
    }

    @Override
    public void deletePost(Long postId) {
        String currUserUuid = UserContext.requireUuid();

        int deleted = postMapper.update(
                null,
                new LambdaUpdateWrapper<Post>()
                        .eq(Post::getId, postId)
                        .eq(Post::getUserUuid, currUserUuid)
                        .eq(Post::getStatus, STATUS_NORMAL)
                        .set(Post::getStatus, STATUS_DELETED)
                        .set(Post::getDeletedAt, LocalDateTime.now())
        );
        if(deleted == 0) {
            throw new RuntimeException("posts not found or no permissions.");
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

    private void validateContent(String content) {
        if (content.isEmpty()) {
            throw new RuntimeException("Content cannot be blank.");
        }
        if (content.length() > MAX_POST_CONTENT_LENGTH) {
            throw new RuntimeException("Content too long.");
        }
    }
}
