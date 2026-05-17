package com.marmot.qilu.modules.admin.user.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.modules.admin.user.dto.AdminUserPageQueryDTO;
import com.marmot.qilu.modules.admin.user.service.AdminUserService;
import com.marmot.qilu.modules.admin.user.vo.AdminUserPageItemVO;
import com.marmot.qilu.modules.admin.user.vo.AdminUserPageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AdminUser", description = "用户管理模块")
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    @Operation(summary = "分页查询用户")
    public ApiResponse<AdminUserPageVO<AdminUserPageItemVO>> getUserPage(AdminUserPageQueryDTO dto) {
        return ApiResponse.success(adminUserService.getUserPage(dto));
    }

    @PatchMapping("/{userUuid}/ban")
    @Operation(summary = "封禁用户")
    public ApiResponse<Void> banUser(@PathVariable String userUuid) {
        adminUserService.banUser(userUuid);
        return ApiResponse.success();
    }

    @PatchMapping("/{userUuid}/unban")
    @Operation(summary = "解封用户")
    public ApiResponse<Void> unbanUser(@PathVariable String userUuid) {
        adminUserService.unbanUser(userUuid);
        return ApiResponse.success();
    }
}
