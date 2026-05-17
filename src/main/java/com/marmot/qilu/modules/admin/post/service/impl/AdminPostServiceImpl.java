package com.marmot.qilu.modules.admin.post.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.modules.admin.post.dto.AdminPostPageQueryDTO;
import com.marmot.qilu.modules.admin.post.service.AdminPostService;
import com.marmot.qilu.modules.admin.post.vo.AdminPostPageItemVO;
import com.marmot.qilu.modules.admin.post.vo.AdminPostPageVO;
import com.marmot.qilu.modules.post.entity.Post;
import com.marmot.qilu.modules.post.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.trimToNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminPostServiceImpl implements AdminPostService {

    private static final int STATUS_NORMAL = 1;
    private static final int STATUS_BANNED = 2;

    private final PostMapper postMapper;

    @Override
    public AdminPostPageVO<AdminPostPageItemVO> getPostPage(AdminPostPageQueryDTO dto) {
        if(dto == null) {
            throw new BadRequestException("post page query dto is invalid");
        }

        LambdaQueryWrapper<Post> queryWrapper = new LambdaQueryWrapper<Post>()
                .eq(dto.getPostId() != null, Post::getId, dto.getPostId())
                .eq(dto.getRootId() != null, Post::getRootId, dto.getRootId())
                .eq(StringUtils.hasText(dto.getUserUuid()), Post::getUserUuid, trimToNull(dto.getUserUuid()))
                .like(StringUtils.hasText(dto.getTitle()), Post::getTitle, trimToNull(dto.getTitle()))
                .eq(dto.getStatus() != null, Post::getStatus, dto.getStatus())
                .eq(dto.getVisibility() != null, Post::getVisibility, dto.getVisibility())
                .orderByDesc(Post::getCreatedAt);

        Page<Post> page = postMapper.selectPage(
                Page.of(dto.getCurrent(), dto.getSize()),
                queryWrapper
        );

        List<AdminPostPageItemVO> records = page.getRecords()
                .stream()
                .map(this::toPageItemVO)
                .toList();

        return new AdminPostPageVO<>(
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                records
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void banPost(Long postId) {
        validatePostId(postId);

        int updated = postMapper.update(
                null,
                new LambdaUpdateWrapper<Post>()
                        .eq(Post::getId, postId)
                        .set(Post::getStatus, STATUS_BANNED)
        );

        if (updated > 1) {
            throw new IllegalStateException("ban post failed");
        }

        log.info("ban post success, postId={}", postId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbanPost(Long postId) {
        validatePostId(postId);

        int updated = postMapper.update(
                null,
                new LambdaUpdateWrapper<Post>()
                        .eq(Post::getId, postId)
                        .set(Post::getStatus, STATUS_NORMAL)
        );

        if (updated > 1) {
            throw new IllegalStateException("unban post failed");
        }

        log.info("unban post success, postId={}", postId);
    }

    private void validatePostId(Long postId) {
        if (postId == null || postId <= 0) {
            throw new BadRequestException("post id is invalid");
        }
    }

    private AdminPostPageItemVO toPageItemVO(Post post) {
        AdminPostPageItemVO vo = new AdminPostPageItemVO();
        vo.setId(post.getId());
        vo.setParentId(post.getParentId());
        vo.setRootId(post.getRootId());
        vo.setBranchPrompt(post.getBranchPrompt());
        vo.setUserUuid(post.getUserUuid());
        vo.setTitle(post.getTitle());
        vo.setContentSnippet(post.getContentSnippet());
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setStatus(post.getStatus());
        vo.setVisibility(post.getVisibility());
        vo.setCreatedAt(post.getCreatedAt());
        vo.setUpdatedAt(post.getUpdatedAt());
        vo.setDeletedAt(post.getDeletedAt());
        return vo;
    }


}
