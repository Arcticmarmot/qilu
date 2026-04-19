package com.marmot.qilu.modules.notification.controller;

import com.marmot.qilu.common.event.interaction.InteractionEntityType;
import com.marmot.qilu.common.event.interaction.InteractionEventType;
import com.marmot.qilu.common.result.Result;
import com.marmot.qilu.modules.notification.service.NotificationService;
import com.marmot.qilu.modules.notification.vo.NotificationListItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "通知模块", description = "通知查询与已读管理相关接口")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
            summary = "按类型查询通知列表",
            description = "查询当前登录用户指定类型的通知列表，例如 POST_LIKED、POST_COMMENTED。该接口只负责查询，不会修改通知状态。"
    )
    @GetMapping
    public Result<List<NotificationListItemVO>> listNotification(@RequestParam InteractionEventType type) {
        return Result.success(notificationService.listNotificationsByType(type));
    }

    @Operation(
            summary = "按类型批量标记通知为已读",
            description = "将当前登录用户指定类型的所有未读通知批量标记为已读，例如将所有 POST_LIKED 通知设为已读。"
    )
    @PatchMapping("/read-all")
    public Result<Void> markAllRead(@RequestParam InteractionEventType type) {
        notificationService.markNotificationsReadByType(type);
        return Result.success();
    }
}
