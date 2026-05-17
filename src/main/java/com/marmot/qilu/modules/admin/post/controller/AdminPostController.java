package com.marmot.qilu.modules.admin.post.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.modules.admin.post.dto.AdminPostPageQueryDTO;
import com.marmot.qilu.modules.admin.post.service.AdminPostService;
import com.marmot.qilu.modules.admin.post.vo.AdminPostPageItemVO;
import com.marmot.qilu.modules.admin.post.vo.AdminPostPageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AdminPost", description = "帖子管理模块")
@RestController
@RequestMapping("/admin/posts")
@RequiredArgsConstructor
public class AdminPostController {

    private final AdminPostService adminPostService;

    @GetMapping
    @Operation(summary = "分页查询帖子")
    public ApiResponse<AdminPostPageVO<AdminPostPageItemVO>> getPostPage(AdminPostPageQueryDTO dto) {
        return ApiResponse.success(adminPostService.getPostPage(dto));
    }

    @PatchMapping("/{postId}/ban")
    @Operation(summary = "封禁帖子")
    public ApiResponse<Void> banPost(@PathVariable Long postId) {
        adminPostService.banPost(postId);
        return ApiResponse.success();
    }

    @PatchMapping("/{postId}/unban")
    @Operation(summary = "解封帖子")
    public ApiResponse<Void> unbanPost(@PathVariable Long postId) {
        adminPostService.unbanPost(postId);
        return ApiResponse.success();
    }
}
