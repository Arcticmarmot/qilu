package com.marmot.qilu.modules.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.marmot.qilu.common.context.AdminContext;
import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.common.exception.ForbiddenException;
import com.marmot.qilu.common.exception.UnauthorizedException;
import com.marmot.qilu.common.security.JwtUtil;
import com.marmot.qilu.modules.admin.dto.AdminLoginDTO;
import com.marmot.qilu.modules.admin.entity.Admin;
import com.marmot.qilu.modules.admin.mapper.AdminMapper;
import com.marmot.qilu.modules.admin.service.AdminAuthService;
import com.marmot.qilu.modules.admin.vo.AdminLoginVO;
import com.marmot.qilu.modules.admin.vo.AdminVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private static final int STATUS_NORMAL = 1;

    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public AdminLoginVO login(AdminLoginDTO dto) {
        validateAdminLoginDTO(dto);

        String username = dto.getUsername();
        String password = dto.getPassword();
        Admin admin = adminMapper.selectOne(
                Wrappers.<Admin>lambdaQuery()
                        .eq(Admin::getUsername, username)
        );

        if (admin == null) {
            throw new UnauthorizedException("username or password is incorrect");
        }

        if (admin.getStatus() == null || admin.getStatus() != STATUS_NORMAL) {
            throw new ForbiddenException("admin user is disabled");
        }

        boolean matched = passwordEncoder.matches(password, admin.getPasswordHash());

        if (!matched) {
            throw new UnauthorizedException("username or password is incorrect");
        }

        String token = jwtUtil.generateAdminToken(admin.getUuid());

        AdminLoginVO vo = new AdminLoginVO();
        vo.setToken(token);
        vo.setUuid(admin.getUuid());
        vo.setRole(admin.getRole());
        vo.setUsername(admin.getUsername());

        log.info("admin login success, userUuid={}, username={}, role={}",
                admin.getUuid(), admin.getUsername(), admin.getRole());

        return vo;
    }

    @Override
    public AdminVO getCurrentAdminProfile() {
        String adminUuid = AdminContext.requireUuid();
        Admin admin = adminMapper.selectOne(
                Wrappers.<Admin>lambdaQuery()
                        .eq(Admin::getUuid, adminUuid)
        );

        AdminVO vo = new AdminVO();
        vo.setUuid(admin.getUuid());
        vo.setUsername(admin.getUsername());
        vo.setRole(admin.getRole());
        vo.setStatus(admin.getStatus());
        vo.setCreateAt(admin.getCreatedAt());
        return vo;
    }

    private void validateAdminLoginDTO(AdminLoginDTO dto) {
        if (dto == null) {
            throw new BadRequestException("request body must not be null");
        }

        String username = dto.getUsername();
        String password = dto.getPassword();

        if (username == null || username.isBlank()) {
            throw new BadRequestException("username must not be blank");
        }

        if (password == null || password.isBlank()) {
            throw new BadRequestException("password must not be blank");
        }
    }
}
