package com.marmot.qilu.modules.notification.reply.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "回复通知列表项")
public class ReplyNotificationListItemVO {

    @Schema(description = "通知ID", example = "1")
    private Long id;

    @Schema(description = "回复ID", example = "1")
    private Long replyId;

    @Schema(description = "触发动作的用户UUID")
    private String actorUuid;

    @Schema(description = "触发动作的用户昵称")
    private String actorNickname;

    @Schema(description = "关联实体类型，例如 COMMENT REPLY")
    private String entityType;

    @Schema(description = "关联实体ID", example = "1001")
    private Long entityId;

    @Schema(description = "关联实体预览")
    private String entityPreview;

    @Schema(description = "回复内容预览")
    private String contentPreview;

    @Schema(description = "是否已读：0未读 1已读", example = "0")
    private Integer isRead;

    @Schema(description = "通知创建时间")
    private LocalDateTime createdAt;
}
