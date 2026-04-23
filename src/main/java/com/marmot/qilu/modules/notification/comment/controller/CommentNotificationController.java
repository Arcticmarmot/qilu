package com.marmot.qilu.modules.notification.comment.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.modules.notification.comment.service.CommentNotificationService;
import com.marmot.qilu.modules.notification.comment.vo.CommentNotificationListItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "CommentNotification", description = "评论通知查询与已读管理相关接口")
@RestController
@RequestMapping("/comment-notifications")
@RequiredArgsConstructor
public class CommentNotificationController {

    private final CommentNotificationService commentNotificationService;

    @Operation(
            summary = "按类型查询通知列表",
            description = "查询当前登录用户评论的通知列表。该接口只负责查询，不会修改通知状态。"
    )
    @GetMapping
    public ApiResponse<List<CommentNotificationListItemVO>> listCommentNotifications() {
        return ApiResponse.success(commentNotificationService.listCommentNotifications());
    }

    @Operation(
            summary = "按类型批量标记通知为已读",
            description = "将当前登录用户评论的所有未读通知批量标记为已读"
    )
    @PatchMapping("/read-all")
    public ApiResponse<Void> markCommentNotificationsRead() {
        commentNotificationService.markCommentNotificationsRead();
        return ApiResponse.success();
    }

    @Operation(
            summary = "获取当前未读通知数量",
            description = "当前登录用户未读评论通知的数量"
    )
    @GetMapping("/unread-count")
    public ApiResponse<Integer> getUnreadCommentNotificationsCount() {
        return ApiResponse.success(commentNotificationService.getUnreadCommentNotificationCount());
    }
}
