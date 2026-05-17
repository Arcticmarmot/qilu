package com.marmot.qilu.modules.user.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.modules.user.dto.UserCreateDTO;
import com.marmot.qilu.modules.user.service.UserService;
import com.marmot.qilu.modules.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "用户模块：当前登录用户信息查询与用户资料维护")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "注册用户", description = "根据邮箱和密码注册新用户")
    @PostMapping
    public ApiResponse<UserVO> createUser(@RequestBody UserCreateDTO dto){
        return ApiResponse.success(userService.createUser(dto));
    }

    @Operation(summary = "获取用户公开信息", description = "根据用户UUID获取该用户的公开资料")
    @GetMapping("/{uuid}")
    public ApiResponse<UserVO> getUserProfile(@PathVariable String uuid) {
        return ApiResponse.success(userService.getUserProfile(uuid));
    }

    @Operation(summary = "获取当前登录用户信息", description = "返回当前访问令牌对应的用户基础信息")
    @GetMapping("/me")
    public ApiResponse<UserVO> getCurrentUser() {
        return ApiResponse.success(userService.getCurrentUserProfile());
    }
}