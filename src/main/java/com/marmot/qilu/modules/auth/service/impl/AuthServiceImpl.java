package com.marmot.qilu.modules.auth.service.impl;

import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.common.exception.ForbiddenException;
import com.marmot.qilu.common.exception.UnauthorizedException;
import com.marmot.qilu.common.security.JwtUtil;
import com.marmot.qilu.modules.auth.dto.LoginDTO;
import com.marmot.qilu.modules.auth.service.AuthService;
import com.marmot.qilu.modules.auth.vo.LoginVO;
import com.marmot.qilu.modules.user.entity.User;
import com.marmot.qilu.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final int STATUS_ENABLED = 1;

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO login(LoginDTO dto) {
        validateLoginDTO(dto);

        User user = userService.getUserByEmail(dto.getEmail());

        if (user == null) {
            throw new UnauthorizedException("email or password is incorrect");
        }

        if (user.getStatus() == null || user.getStatus() != STATUS_ENABLED) {
            throw new ForbiddenException("user is disabled");
        }

        boolean matched = passwordEncoder.matches(dto.getPassword(), user.getPasswordHash());

        if (!matched) {
            throw new UnauthorizedException("email or password is incorrect");
        }

        String token = jwtUtil.generateToken(user.getUuid(), user.getEmail());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUuid(user.getUuid());
        vo.setEmail(user.getEmail());
        vo.setNickname(user.getNickname());

        log.info("login success, userUuid={}, email={}", user.getUuid(), user.getEmail());

        return vo;
    }

    private void validateLoginDTO(LoginDTO dto) {
        if (dto == null) {
            throw new BadRequestException("request body must not be null");
        }

        String email = dto.getEmail();
        String password = dto.getPassword();

        if (email == null || email.isBlank()) {
            throw new BadRequestException("email must not be blank");
        }

        if (password == null || password.isBlank()) {
            throw new BadRequestException("password must not be blank");
        }
    }
}