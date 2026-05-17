package com.marmot.qilu.modules.admin.reply.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.modules.admin.reply.dto.AdminReplyPageQueryDTO;
import com.marmot.qilu.modules.admin.reply.service.AdminReplyService;
import com.marmot.qilu.modules.admin.reply.vo.AdminReplyPageItemVO;
import com.marmot.qilu.modules.admin.reply.vo.AdminReplyPageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AdminReply", description = "回复管理模块")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/replies")
public class AdminReplyController {

    private final AdminReplyService adminReplyService;

    @GetMapping
    @Operation(summary = "分页查询回复")
    public ApiResponse<AdminReplyPageVO<AdminReplyPageItemVO>> getReplyPage(AdminReplyPageQueryDTO dto) {
        return ApiResponse.success(adminReplyService.getReplyPage(dto));
    }

    @PatchMapping("/{replyId}/ban")
    @Operation(summary = "封禁回复")
    public ApiResponse<Void> banReply(@PathVariable Long replyId) {
        adminReplyService.banReply(replyId);
        return ApiResponse.success();
    }

    @PatchMapping("/{replyId}/unban")
    @Operation(summary = "解封回复")
    public ApiResponse<Void> unbanReply(@PathVariable Long replyId) {
        adminReplyService.unbanReply(replyId);
        return ApiResponse.success();
    }
}