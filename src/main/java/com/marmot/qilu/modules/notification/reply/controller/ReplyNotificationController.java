package com.marmot.qilu.modules.notification.reply.controller;

import com.marmot.qilu.common.result.ApiResponse;
import com.marmot.qilu.modules.notification.reply.service.ReplyNotificationService;
import com.marmot.qilu.modules.notification.reply.vo.ReplyNotificationListItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "ReplyNotification", description = "回复通知查询与已读管理相关接口")
@RestController
@RequestMapping("/reply-notifications")
@RequiredArgsConstructor
public class ReplyNotificationController {

    private final ReplyNotificationService replyNotificationService;

    @Operation(
            summary = "按类型查询通知列表",
            description = "查询当前登录用户回复的通知列表。该接口只负责查询，不会修改通知状态。"
    )
    @GetMapping
    public ApiResponse<List<ReplyNotificationListItemVO>> listReplyNotifications() {
        return ApiResponse.success(replyNotificationService.listReplyNotifications());
    }

    @Operation(
            summary = "按类型批量标记通知为已读",
            description = "将当前登录用户回复的所有未读通知批量标记为已读"
    )
    @PatchMapping("/read-all")
    public ApiResponse<Void> markCommentNotificationsRead() {
        replyNotificationService.markReplyNotificationRead();
        return ApiResponse.success();
    }

    @Operation(
            summary = "获取当前未读通知数量",
            description = "当前登录用户未读回复通知的数量"
    )
    @GetMapping("/unread-count")
    public ApiResponse<Integer> getUnreadReplyNotificationsCount() {
        return ApiResponse.success(replyNotificationService.getUnreadReplyNotificationCount());
    }
}
