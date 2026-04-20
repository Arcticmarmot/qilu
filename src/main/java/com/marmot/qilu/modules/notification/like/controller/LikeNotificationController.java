package com.marmot.qilu.modules.notification.like.controller;

import com.marmot.qilu.common.result.Result;
import com.marmot.qilu.modules.notification.like.service.LikeNotificationService;
import com.marmot.qilu.modules.notification.like.vo.LikeNotificationListItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "点赞通知模块", description = "点赞通知查询与已读管理相关接口")
@RestController
@RequestMapping("/like-notifications")
@RequiredArgsConstructor
public class LikeNotificationController {

    private final LikeNotificationService likeNotificationService;

    @Operation(
            summary = "按类型查询通知列表",
            description = "查询当前登录用户点赞的通知列表。该接口只负责查询，不会修改通知状态。"
    )
    @GetMapping
    public Result<List<LikeNotificationListItemVO>> listNotification() {
        return Result.success(likeNotificationService.listLikeNotifications());
    }

    @Operation(
            summary = "按类型批量标记通知为已读",
            description = "将当前登录用户点赞的所有未读通知批量标记为已读"
    )
    @PatchMapping("/read-all")
    public Result<Void> markAllRead() {
        likeNotificationService.markLikeNotificationsRead();
        return Result.success();
    }
}
