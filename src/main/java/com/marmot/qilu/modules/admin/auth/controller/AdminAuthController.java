package com.marmot.qilu.modules.admin.auth.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.modules.admin.auth.dto.OperatorLoginDTO;
import com.marmot.qilu.modules.admin.auth.service.AdminAuthService;
import com.marmot.qilu.modules.admin.auth.vo.OperatorLoginVO;
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
    public ApiResponse<OperatorLoginVO> login(@RequestBody OperatorLoginDTO dto) {
        return ApiResponse.success(adminAuthService.login(dto));
    }
}
