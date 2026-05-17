package com.marmot.qilu.modules.admin.comment.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.modules.admin.comment.dto.AdminCommentPageQueryDTO;
import com.marmot.qilu.modules.admin.comment.service.AdminCommentService;
import com.marmot.qilu.modules.admin.comment.vo.AdminCommentPageItemVO;
import com.marmot.qilu.modules.admin.comment.vo.AdminCommentPageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AdminComment", description = "评论管理模块")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/comments")
public class AdminCommentController {

    private final AdminCommentService adminCommentService;

    @GetMapping
    @Operation(summary = "分页查询评论")
    public ApiResponse<AdminCommentPageVO<AdminCommentPageItemVO>> getCommentPage(AdminCommentPageQueryDTO dto) {
        return ApiResponse.success(adminCommentService.getCommentPage(dto));
    }

    @PatchMapping("/{commentId}/ban")
    @Operation(summary = "封禁评论")
    public ApiResponse<Void> banComment(@PathVariable Long commentId) {
        adminCommentService.banComment(commentId);
        return ApiResponse.success();
    }

    @PatchMapping("/{commentId}/unban")
    @Operation(summary = "解封评论")
    public ApiResponse<Void> unbanComment(@PathVariable Long commentId) {
        adminCommentService.unbanComment(commentId);
        return ApiResponse.success();
    }
}