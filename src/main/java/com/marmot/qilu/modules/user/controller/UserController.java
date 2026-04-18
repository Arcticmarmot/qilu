package com.marmot.qilu.modules.user.controller;

import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.result.Result;
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
    public Result<UserVO> createUser(@RequestBody UserCreateDTO dto){
        return Result.success(userService.createUser(dto));
    }

    @Operation(summary = "获取用户公开信息", description = "根据用户UUID获取该用户的公开资料")
    @GetMapping("/{uuid}")
    public Result<UserVO> getUserProfile(@PathVariable String uuid) {
        return Result.success(userService.getUserProfile(uuid));
    }

    @Operation(summary = "获取当前登录用户信息", description = "返回当前访问令牌对应的用户基础信息")
    @GetMapping("/me")
    public Result<UserVO> getCurrentUser() {
        String uuid = UserContext.getUuid();
        return Result.success(userService.getCurrentUserProfile(uuid));
    }
}