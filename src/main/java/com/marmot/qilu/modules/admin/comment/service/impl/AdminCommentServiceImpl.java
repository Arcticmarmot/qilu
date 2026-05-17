package com.marmot.qilu.modules.admin.comment.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.modules.admin.comment.dto.AdminCommentPageQueryDTO;
import com.marmot.qilu.modules.admin.comment.service.AdminCommentService;
import com.marmot.qilu.modules.admin.comment.vo.AdminCommentPageItemVO;
import com.marmot.qilu.modules.admin.comment.vo.AdminCommentPageVO;
import com.marmot.qilu.modules.comment.entity.Comment;
import com.marmot.qilu.modules.comment.mapper.CommentMapper;
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
public class AdminCommentServiceImpl implements AdminCommentService {

    private static final int STATUS_NORMAL = 1;
    private static final int STATUS_BANNED = 2;

    private final CommentMapper commentMapper;

    @Override
    public AdminCommentPageVO<AdminCommentPageItemVO> getCommentPage(AdminCommentPageQueryDTO dto) {
        if(dto == null) {
            throw new BadRequestException("comment page query dto is invalid");
        }

        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<Comment>()
                .eq(dto.getId() != null, Comment::getId, dto.getId())
                .eq(dto.getPostId() != null, Comment::getPostId, dto.getPostId())
                .eq(StringUtils.hasText(dto.getUserUuid()), Comment::getUserUuid, trimToNull(dto.getUserUuid()))
                .like(StringUtils.hasText(dto.getContent()), Comment::getContent, trimToNull(dto.getContent()))
                .eq(dto.getStatus() != null, Comment::getStatus, dto.getStatus())
                .orderByDesc(Comment::getCreatedAt);

        Page<Comment> page = commentMapper.selectPage(
                Page.of(dto.getCurrent(), dto.getSize()),
                queryWrapper
        );

        List<AdminCommentPageItemVO> records = page.getRecords()
                .stream()
                .map(this::toPageItemVO)
                .toList();

        return new AdminCommentPageVO<>(
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                records
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void banComment(Long commentId) {
        validateCommentId(commentId);

        int updated = commentMapper.update(
                null,
                new LambdaUpdateWrapper<Comment>()
                        .eq(Comment::getId, commentId)
                        .set(Comment::getStatus, STATUS_BANNED)
        );

        if (updated > 1) {
            throw new IllegalStateException("ban comment failed");
        }

        log.info("ban comment success, commentId={}", commentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbanComment(Long commentId) {
        validateCommentId(commentId);

        int updated = commentMapper.update(
                null,
                new LambdaUpdateWrapper<Comment>()
                        .eq(Comment::getId, commentId)
                        .set(Comment::getStatus, STATUS_NORMAL)
        );

        if (updated > 1) {
            throw new IllegalStateException("unban comment failed");
        }

        log.info("unban comment success, commentId={}", commentId);
    }

    private void validateCommentId(Long commentId) {
        if (commentId == null || commentId <= 0) {
            throw new BadRequestException("comment id is invalid");
        }
    }

    private AdminCommentPageItemVO toPageItemVO(Comment comment) {
        AdminCommentPageItemVO vo = new AdminCommentPageItemVO();
        vo.setId(comment.getId());
        vo.setPostId(comment.getPostId());
        vo.setPostAuthorUuid(comment.getPostAuthorUuid());
        vo.setUserUuid(comment.getUserUuid());
        vo.setContent(comment.getContent());
        vo.setLikeCount(comment.getLikeCount());
        vo.setReplyCount(comment.getReplyCount());
        vo.setStatus(comment.getStatus());
        vo.setCreatedAt(comment.getCreatedAt());
        vo.setUpdatedAt(comment.getUpdatedAt());
        return vo;
    }
}