package com.marmot.qilu.modules.admin.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.modules.admin.dto.AdminLoginDTO;
import com.marmot.qilu.modules.admin.service.AdminAuthService;
import com.marmot.qilu.modules.admin.vo.AdminLoginVO;
import com.marmot.qilu.modules.admin.vo.AdminVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AdminAuth", description = "管理员认证模块")
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @Operation(summary = "管理员登录", description = "管理员使用账号和密码登录，成功后返回访问令牌及基础信息")
    @PostMapping("/login")
    public ApiResponse<AdminLoginVO> login(@RequestBody AdminLoginDTO dto) {
        return ApiResponse.success(adminAuthService.login(dto));
    }

    @Operation(summary = "获取当前登录管理员信息", description = "返回当前访问令牌对应的管理员基础信息")
    @GetMapping("/me")
    public ApiResponse<AdminVO> getCurrentAdminProfile() {
        return ApiResponse.success(adminAuthService.getCurrentAdminProfile());
    }
}
