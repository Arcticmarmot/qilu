package com.marmot.qilu.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.common.exception.ConflictException;
import com.marmot.qilu.common.exception.NotFoundException;
import com.marmot.qilu.modules.user.dto.UserCreateDTO;
import com.marmot.qilu.modules.user.entity.User;
import com.marmot.qilu.modules.user.mapper.UserMapper;
import com.marmot.qilu.modules.user.service.UserService;
import com.marmot.qilu.modules.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final int STATUS_NORMAL = 1;

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO createUser(UserCreateDTO dto) {
        validateCreateUserDTO(dto);

        User existed = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getEmail, dto.getEmail())
                        .last("limit 1")
        );
        if (existed != null) {
            throw new ConflictException("email already exists");
        }

        User user = new User();
        user.setUuid(UUID.randomUUID().toString());
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(STATUS_NORMAL);

        try {
            int inserted = userMapper.insert(user);
            if (inserted != 1) {
                throw new IllegalStateException("create user failed");
            }
        } catch (DuplicateKeyException e) {
            throw new ConflictException("email already exists");
        }

        log.info("create user success, userUuid={}", user.getUuid());

        return toUserVO(user);
    }

    @Override
    public UserVO getUserProfile(String uuid) {
        validateUuid(uuid);

        User user = getUserByUuid(uuid);
        if (user == null) {
            throw new NotFoundException("user not found");
        }
        return toUserVO(user);
    }

    @Override
    public UserVO getCurrentUserProfile(String uuid) {
        validateUuid(uuid);

        User user = getUserByUuid(uuid);
        if (user == null) {
            throw new NotFoundException("current user not found");
        }
        return toUserVO(user);
    }

    @Override
    public User getUserByUuid(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            return null;
        }

        return userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUuid, uuid)
                        .last("limit 1")
        );
    }

    @Override
    public User getUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getEmail, email)
                        .last("limit 1")
        );
    }

    private void validateCreateUserDTO(UserCreateDTO dto) {
        if (dto == null) {
            throw new BadRequestException("request body must not be null");
        }

        String nickname = dto.getNickname();
        String password = dto.getPassword();
        String email = dto.getEmail();

        if (nickname == null || nickname.isBlank()) {
            throw new BadRequestException("nickname must not be blank");
        }

        if (password == null || password.isBlank()) {
            throw new BadRequestException("password must not be blank");
        }

        if (email == null || email.isBlank()) {
            throw new BadRequestException("email must not be blank");
        }

        if (!email.contains("@")) {
            throw new BadRequestException("email is invalid");
        }
    }

    private void validateUuid(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            throw new BadRequestException("uuid must not be blank");
        }
    }

    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        vo.setUuid(user.getUuid());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setStatus(user.getStatus());
        vo.setCreateAt(user.getCreatedAt());
        return vo;
    }
}