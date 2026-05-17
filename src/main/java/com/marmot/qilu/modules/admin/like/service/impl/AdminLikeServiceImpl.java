package com.marmot.qilu.modules.admin.like.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.marmot.qilu.common.event.like.LikeCreationType;
import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.modules.admin.like.dto.AdminLikePageQueryDTO;
import com.marmot.qilu.modules.admin.like.service.AdminLikeService;
import com.marmot.qilu.modules.admin.like.vo.AdminLikePageItemVO;
import com.marmot.qilu.modules.admin.like.vo.AdminLikePageVO;
import com.marmot.qilu.modules.like.entity.Like;
import com.marmot.qilu.modules.like.mapper.LikeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.trimToNull;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminLikeServiceImpl implements AdminLikeService {

    private final LikeMapper likeMapper;

    @Override
    public AdminLikePageVO<AdminLikePageItemVO> getLikePage(AdminLikePageQueryDTO dto) {
        if(dto == null) {
            throw new BadRequestException("like page query dto is invalid");
        }
        LikeCreationType creationType = dto.getCreationType();
        LambdaQueryWrapper<Like> queryWrapper = new LambdaQueryWrapper<Like>()
                .eq(dto.getLikeId() != null, Like::getId, dto.getLikeId())
                .eq(creationType != null && StringUtils.hasText(creationType.toString()),
                        Like::getCreationType, dto.getCreationType())
                .eq(dto.getCreationId() != null, Like::getCreationId, dto.getCreationId())
                .eq(StringUtils.hasText(dto.getUserUuid()), Like::getUserUuid, trimToNull(dto.getUserUuid()))
                .eq(dto.getStatus() != null, Like::getStatus, dto.getStatus())
                .orderByDesc(Like::getCreatedAt);

        Page<Like> page = likeMapper.selectPage(
                Page.of(dto.getCurrent(), dto.getSize()),
                queryWrapper
        );

        List<AdminLikePageItemVO> records = page.getRecords()
                .stream()
                .map(this::toPageItemVO)
                .toList();

        return new AdminLikePageVO<>(
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                records
        );
    }

    private AdminLikePageItemVO toPageItemVO(Like like) {
        AdminLikePageItemVO vo = new AdminLikePageItemVO();
        vo.setId(like.getId());
        vo.setCreationType(like.getCreationType());
        vo.setCreationId(like.getCreationId());
        vo.setUserUuid(like.getUserUuid());
        vo.setStatus(like.getStatus());
        vo.setCreatedAt(like.getCreatedAt());
        vo.setUpdatedAt(like.getUpdatedAt());
        return vo;
    }
}