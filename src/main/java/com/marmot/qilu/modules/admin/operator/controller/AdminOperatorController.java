package com.marmot.qilu.modules.admin.operator.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.modules.admin.operator.service.AdminOperatorService;
import com.marmot.qilu.modules.admin.operator.vo.OperatorVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AdminOperator", description = "管理员信息模块")
@RestController
@RequestMapping("/admin/operators")
@RequiredArgsConstructor
public class AdminOperatorController {

    private final AdminOperatorService adminOperatorService;

    @Operation(summary = "获取当前登录管理员信息", description = "返回当前访问令牌对应的管理员基础信息")
    @GetMapping("/me")
    public ApiResponse<OperatorVO> getCurrentAdminProfile() {
        return ApiResponse.success(adminOperatorService.getCurrentAdminProfile());
    }
}
