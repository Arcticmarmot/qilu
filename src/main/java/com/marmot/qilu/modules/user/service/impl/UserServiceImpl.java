package com.marmot.qilu.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.marmot.qilu.modules.user.dto.CreateUserDTO;
import com.marmot.qilu.modules.user.entity.User;
import com.marmot.qilu.modules.user.mapper.UserMapper;
import com.marmot.qilu.modules.user.service.UserService;
import com.marmot.qilu.modules.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserVO createUser(CreateUserDTO dto) {
        if(dto.getNickname() == null || dto.getNickname().isBlank()) {
            throw new RuntimeException("nickname couldn't be null");
        }
        if(dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new RuntimeException("password couldn't be null");
        }
        if(dto.getEmail() != null && !dto.getEmail().isBlank()) {
            User existed = userMapper.selectOne(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getEmail, dto.getEmail())
            );
            if(existed != null) {
                throw new RuntimeException("email already existed");
            }
        }
        User user = new User();
        user.setUuid(UUID.randomUUID().toString().replace("-", ""));
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(1);
        userMapper.insert(user);
        return toVO(user);
    }

    @Override
    public UserVO getUserByUuid(String uuid) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUuid, uuid)
        );
        if(user == null) {
            throw new RuntimeException("user not exist");
        }
        return toVO(user);
    }

    private UserVO toVO(User user) {
        UserVO vo = new UserVO();
        vo.setUuid(user.getUuid());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setStatus(user.getStatus());
        vo.setCreateAt(user.getCreatedAt());
        return vo;
    }
}
