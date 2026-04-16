package com.marmot.qilu.modules.post.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.modules.post.dto.PostCreateDTO;
import com.marmot.qilu.modules.post.dto.PostPageQueryDTO;
import com.marmot.qilu.modules.post.dto.PostUpdateDTO;
import com.marmot.qilu.modules.post.entity.Post;
import com.marmot.qilu.modules.post.mapper.PostMapper;
import com.marmot.qilu.modules.post.service.PostService;
import com.marmot.qilu.modules.post.vo.PostDetailVO;
import com.marmot.qilu.modules.post.vo.PostPageItemVO;
import com.marmot.qilu.modules.post.vo.PostPageVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PostServiceImpl implements PostService {

    private static final int STATUS_DELETED = 0;
    private static final int STATUS_NORMAL = 1;
    private static final int VISIBILITY_PUBLIC = 1;
    private static final int VISIBILITY_PRIVATE = 2;

    private final PostMapper postMapper;

    public PostServiceImpl(PostMapper postMapper) {
        this.postMapper = postMapper;
    }

    @Override
    public Long createPost(PostCreateDTO dto) {
        String currUserUuid = UserContext.getUuid();
        if(currUserUuid == null) {
            throw new RuntimeException("User not logged in.");
        }
        Post post = new Post();
        post.setUserUuid(currUserUuid);
        post.setTitle(dto.getTitle());
        post.setContent(dto.getContent());
        post.setVisibility(dto.getVisibility());
        // audit relevant
        post.setStatus(STATUS_NORMAL);
        postMapper.insert(post);
        return post.getId();
    }

    @Override
    public PostDetailVO getPostDetail(Long postId) {
        Post post = postMapper.selectOne(
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getId, postId)
                        .eq(Post::getStatus, STATUS_NORMAL)
        );

        if(post == null) {
            throw new RuntimeException("Post not found.");
        }

        PostDetailVO vo = new PostDetailVO();
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());
        vo.setId(post.getId());
        vo.setCreateAt(post.getCreatedAt());
        vo.setUserUuid(post.getUserUuid());
        return vo;
    }

    @Override
    public void updatePost(Long postId, PostUpdateDTO dto) {
        String currUserUuid = UserContext.getUuid();
        if(currUserUuid == null) {
            throw new RuntimeException("User not logged in.");
        }

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
        String currUserUuid = UserContext.getUuid();
        if(currUserUuid == null) {
            throw new RuntimeException("User not logged in.");
        }

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
    public PostPageVO<PostPageItemVO> getPostPage(PostPageQueryDTO dto) {
        String currUserUuid = UserContext.getUuid();
        if(currUserUuid == null) {
            throw new RuntimeException("User not logged in.");
        }
        long current = dto.getCurrent();
        long size = dto.getSize();

        Page<Post> page = new Page<>(current, size);
        Page<Post> postPage = postMapper.selectPage(
                page,
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getStatus, STATUS_NORMAL)
                        .eq(Post::getVisibility, VISIBILITY_PUBLIC)
                        .orderByDesc(Post::getCreatedAt)
        );

        PostPageVO<PostPageItemVO> pageVO = new PostPageVO<>();
        pageVO.setCurrent(postPage.getCurrent());
        pageVO.setSize(postPage.getSize());
        pageVO.setTotal(postPage.getTotal());
        pageVO.setRecords(postPage.getRecords()
                .stream()
                .map(this::toPostPageItemVO)
                .toList()
        );
        return pageVO;
    }

    private PostPageItemVO toPostPageItemVO(Post post) {
        PostPageItemVO vo = new PostPageItemVO();
        vo.setId(post.getId());
        vo.setTitle(post.getTitle());
        vo.setUserUuid(post.getUserUuid());
        vo.setContentPreview(buildContentPreview(post.getContent()));
        vo.setCreateAt(post.getCreatedAt());
        return vo;
    }

    private String buildContentPreview(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String trimmed = content.trim();
        return trimmed.length() <= 20 ? trimmed : trimmed.substring(0, 20);
    }
}
