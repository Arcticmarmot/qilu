package com.marmot.qilu.modules.admin.auth.service.impl;

import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.common.exception.ForbiddenException;
import com.marmot.qilu.common.exception.UnauthorizedException;
import com.marmot.qilu.common.security.JwtUtil;
import com.marmot.qilu.modules.admin.auth.dto.OperatorLoginDTO;
import com.marmot.qilu.modules.admin.operator.entity.Operator;
import com.marmot.qilu.modules.admin.auth.service.AdminAuthService;
import com.marmot.qilu.modules.admin.auth.vo.OperatorLoginVO;
import com.marmot.qilu.modules.admin.operator.service.AdminOperatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {

    private static final int STATUS_NORMAL = 1;

    private final AdminOperatorService adminOperatorService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public OperatorLoginVO login(OperatorLoginDTO dto) {
        validateOperatorLoginDTO(dto);

        String username = dto.getUsername();
        String password = dto.getPassword();
        Operator operator = adminOperatorService.getOperatorByUsername(username);

        if (operator == null) {
            throw new UnauthorizedException("username or password is incorrect");
        }

        if (operator.getStatus() == null || operator.getStatus() != STATUS_NORMAL) {
            throw new ForbiddenException("admin user is disabled");
        }

        boolean matched = passwordEncoder.matches(password, operator.getPasswordHash());

        if (!matched) {
            throw new UnauthorizedException("username or password is incorrect");
        }

        String token = jwtUtil.generateOperatorToken(operator.getUuid());

        OperatorLoginVO vo = new OperatorLoginVO();
        vo.setToken(token);
        vo.setUuid(operator.getUuid());
        vo.setRole(operator.getRole());
        vo.setUsername(operator.getUsername());

        log.info("operator login success, userUuid={}, username={}, role={}",
                operator.getUuid(), operator.getUsername(), operator.getRole());

        return vo;
    }

    private void validateOperatorLoginDTO(OperatorLoginDTO dto) {
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
