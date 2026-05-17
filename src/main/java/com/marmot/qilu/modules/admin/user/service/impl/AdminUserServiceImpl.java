package com.marmot.qilu.modules.admin.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.modules.admin.user.dto.AdminUserPageQueryDTO;
import com.marmot.qilu.modules.admin.user.service.AdminUserService;
import com.marmot.qilu.modules.admin.user.vo.AdminUserPageVO;
import com.marmot.qilu.modules.admin.user.vo.AdminUserPageItemVO;
import com.marmot.qilu.modules.user.entity.User;
import com.marmot.qilu.modules.user.mapper.UserMapper;
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
public class AdminUserServiceImpl implements AdminUserService {

    private static final int STATUS_DISABLED = 0;
    private static final int STATUS_NORMAL = 1;

    private final UserMapper userMapper;

    @Override
    public AdminUserPageVO<AdminUserPageItemVO> getUserPage(AdminUserPageQueryDTO dto) {
        if(dto == null) {
            throw new BadRequestException("user query dto is invalid");
        }

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>()
                .eq(StringUtils.hasText(dto.getUuid()), User::getUuid, trimToNull(dto.getUuid()))
                .like(StringUtils.hasText(dto.getNickname()), User::getNickname, trimToNull(dto.getNickname()))
                .like(StringUtils.hasText(dto.getEmail()), User::getEmail, trimToNull(dto.getEmail()))
                .eq(dto.getStatus() != null, User::getStatus, dto.getStatus())
                .orderByDesc(User::getCreatedAt);

        Page<User> page = userMapper.selectPage(
                Page.of(dto.getCurrent(), dto.getSize()),
                queryWrapper
        );

        List<AdminUserPageItemVO> records = page.getRecords()
                .stream()
                .map(this::toPageItemVO)
                .toList();

        return new AdminUserPageVO<>(
                page.getCurrent(),
                page.getSize(),
                page.getTotal(),
                records
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void banUser(String userUuid) {
        validateUserUuid(userUuid);

        int updated = userMapper.update(
                null,
                new LambdaUpdateWrapper<User>()
                        .eq(User::getUuid, userUuid)
                        .set(User::getStatus, STATUS_DISABLED)
        );

        if (updated > 1) {
            throw new IllegalStateException("ban user failed");
        }

        log.info("ban user success, userUuid={}", userUuid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbanUser(String userUuid) {
        validateUserUuid(userUuid);

        int updated = userMapper.update(
                null,
                new LambdaUpdateWrapper<User>()
                        .eq(User::getUuid, userUuid)
                        .set(User::getStatus, STATUS_NORMAL)
        );

        if (updated > 1) {
            throw new IllegalStateException("unban user failed");
        }

        log.info("unban user success, userUuid={}", userUuid);
    }

    private void validateUserUuid(String userUuid) {
        if(userUuid == null || userUuid.isBlank()) {
            throw new BadRequestException("user uuid is invalid");
        }
    }


    private AdminUserPageItemVO toPageItemVO(User user) {
        AdminUserPageItemVO vo = new AdminUserPageItemVO();
        vo.setId(user.getId());
        vo.setUuid(user.getUuid());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());
        vo.setUpdatedAt(user.getUpdatedAt());
        return vo;
    }
}
