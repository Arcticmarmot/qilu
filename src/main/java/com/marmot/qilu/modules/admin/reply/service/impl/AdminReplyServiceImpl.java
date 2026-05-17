package com.marmot.qilu.modules.admin.reply.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.modules.admin.reply.dto.AdminReplyPageQueryDTO;
import com.marmot.qilu.modules.admin.reply.service.AdminReplyService;
import com.marmot.qilu.modules.admin.reply.vo.AdminReplyPageItemVO;
import com.marmot.qilu.modules.admin.reply.vo.AdminReplyPageVO;
import com.marmot.qilu.modules.reply.entity.Reply;
import com.marmot.qilu.modules.reply.mapper.ReplyMapper;
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
public class AdminReplyServiceImpl implements AdminReplyService {

    private static final int STATUS_NORMAL = 1;
    private static final int STATUS_BANNED = 2;

    private final ReplyMapper replyMapper;

    @Override
    public AdminReplyPageVO<AdminReplyPageItemVO> getReplyPage(AdminReplyPageQueryDTO dto) {
        if(dto == null) {
            throw new BadRequestException("reply page query dto is invalid");
        }

        LambdaQueryWrapper<Reply> queryWrapper = new LambdaQueryWrapper<Reply>()
                .eq(dto.getId() != null, Reply::getId, dto.getId())
                .eq(dto.getPostId() != null, Reply::getPostId, dto.getPostId())
                .eq(dto.getRootCommentId() != null, Reply::getRootCommentId, dto.getRootCommentId())
                .eq(dto.getParentReplyId() != null, Reply::getParentReplyId, dto.getParentReplyId())
                .eq(StringUtils.hasText(dto.getUserUuid()), Reply::getUserUuid, trimToNull(dto.getUserUuid()))
                .eq(StringUtils.hasText(dto.getTargetUserUuid()), Reply::getTargetUserUuid, trimToNull(dto.getTargetUserUuid()))
                .like(StringUtils.hasText(dto.getContent()), Reply::getContent, trimToNull(dto.getContent()))
                .eq(dto.getStatus() != null, Reply::getStatus, dto.getStatus())
                .orderByDesc(Reply::getCreatedAt);

        Page<Reply> page = replyMapper.selectPage(
                Page.of(dto.getCurrent(), dto.getSize()),
                queryWrapper
        );

        List<AdminReplyPageItemVO> records = page.getRecords()
                .stream()
                .map(this::toPageItemVO)
                .toList();

        return new AdminReplyPageVO<>(
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                records
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void banReply(Long replyId) {
        validateReplyId(replyId);


        int updated = replyMapper.update(
                null,
                new LambdaUpdateWrapper<Reply>()
                        .eq(Reply::getId, replyId)
                        .set(Reply::getStatus, STATUS_BANNED)
        );

        if (updated > 1) {
            throw new IllegalStateException("ban reply failed");
        }

        log.info("ban reply success, replyId={}", replyId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbanReply(Long replyId) {
        validateReplyId(replyId);

        int updated = replyMapper.update(
                null,
                new LambdaUpdateWrapper<Reply>()
                        .eq(Reply::getId, replyId)
                        .set(Reply::getStatus, STATUS_NORMAL)
        );

        if (updated > 1) {
            throw new IllegalStateException("unban reply failed");
        }

        log.info("unban reply success, replyId={}", replyId);
    }


    private void validateReplyId(Long replyId) {
        if (replyId == null || replyId <= 0) {
            throw new BadRequestException("reply id is invalid");
        }
    }


    private AdminReplyPageItemVO toPageItemVO(Reply reply) {
        AdminReplyPageItemVO vo = new AdminReplyPageItemVO();
        vo.setId(reply.getId());
        vo.setPostId(reply.getPostId());
        vo.setRootCommentId(reply.getRootCommentId());
        vo.setParentReplyId(reply.getParentReplyId());
        vo.setUserUuid(reply.getUserUuid());
        vo.setTargetUserUuid(reply.getTargetUserUuid());
        vo.setContent(reply.getContent());
        vo.setLikeCount(reply.getLikeCount());
        vo.setStatus(reply.getStatus());
        vo.setCreatedAt(reply.getCreatedAt());
        vo.setUpdatedAt(reply.getUpdatedAt());
        return vo;
    }
}