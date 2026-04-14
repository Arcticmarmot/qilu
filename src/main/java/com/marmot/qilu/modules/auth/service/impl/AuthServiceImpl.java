package com.marmot.qilu.modules.auth.service.impl;


import com.marmot.qilu.common.security.JwtUtil;
import com.marmot.qilu.modules.auth.dto.LoginDTO;
import com.marmot.qilu.modules.auth.service.AuthService;
import com.marmot.qilu.modules.auth.vo.LoginVO;
import com.marmot.qilu.modules.user.entity.User;
import com.marmot.qilu.modules.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO login(LoginDTO dto) {
        if(dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new RuntimeException("Email must not be blank.");
        }

        if(dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new RuntimeException("Password must not be blank.");
        }

        User user = userService.getUserByEmail(dto.getEmail());
        if(user == null) {
            throw new RuntimeException("User not found.");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new RuntimeException("User is disabled.");
        }

        boolean matched = passwordEncoder.matches(dto.getPassword(), user.getPasswordHash());

        if(!matched) {
            throw new RuntimeException("Invalid password.");
        }

        String token = jwtUtil.generateToken(user.getUuid(), user.getEmail());

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUuid(user.getUuid());
        vo.setEmail(user.getEmail());
        vo.setNickname(user.getNickname());
        return vo;
    }
}
