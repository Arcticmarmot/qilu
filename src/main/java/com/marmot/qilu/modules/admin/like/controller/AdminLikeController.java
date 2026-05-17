package com.marmot.qilu.modules.admin.like.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.modules.admin.like.dto.AdminLikePageQueryDTO;
import com.marmot.qilu.modules.admin.like.service.AdminLikeService;
import com.marmot.qilu.modules.admin.like.vo.AdminLikePageItemVO;
import com.marmot.qilu.modules.admin.like.vo.AdminLikePageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AdminLike", description = "点赞管理模块")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/likes")
public class AdminLikeController {

    private final AdminLikeService adminLikeService;

    @GetMapping
    @Operation(summary = "分页查询点赞")
    public ApiResponse<AdminLikePageVO<AdminLikePageItemVO>> getLikePage(AdminLikePageQueryDTO dto) {
        return ApiResponse.success(adminLikeService.getLikePage(dto));
    }
}