package com.marmot.qilu.modules.notification.like.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "通知列表项")
public class LikeNotificationListItemVO {

    @Schema(description = "通知ID", example = "1")
    private Long id;

    @Schema(description = "触发动作的用户UUID")
    private String actorUuid;

    @Schema(description = "触发动作的用户昵称")
    private String actorNickname;

    @Schema(description = "关联实体类型，例如 POST")
    private String entityType;

    @Schema(description = "关联实体ID", example = "1001")
    private Long entityId;

    @Schema(description = "关联实体标题预览")
    private String entityTitlePreview;

    @Schema(description = "是否已读：0未读 1已读", example = "0")
    private Integer isRead;

    @Schema(description = "通知创建时间")
    private LocalDateTime createdAt;
}
