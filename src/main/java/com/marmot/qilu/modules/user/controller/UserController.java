package com.marmot.qilu.modules.user.controller;

import com.marmot.qilu.common.result.Result;
import com.marmot.qilu.modules.user.dto.CreateUserDTO;
import com.marmot.qilu.modules.user.entity.User;
import com.marmot.qilu.modules.user.service.UserService;
import com.marmot.qilu.modules.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public Result<UserVO> createUser(@RequestBody CreateUserDTO dto){
        return Result.success(userService.createUser(dto));
    }

    @GetMapping("/{uuid}")
    public Result<UserVO> getUser(@PathVariable String uuid) {
        return Result.success(userService.getUserProfile(uuid));
    }
}