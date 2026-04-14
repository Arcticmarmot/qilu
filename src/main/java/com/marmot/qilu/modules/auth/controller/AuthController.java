package com.marmot.qilu.modules.auth.controller;

import com.marmot.qilu.common.result.Result;
import com.marmot.qilu.modules.auth.dto.LoginDTO;
import com.marmot.qilu.modules.auth.service.AuthService;
import com.marmot.qilu.modules.auth.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

}
