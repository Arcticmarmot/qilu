package com.marmot.qilu.modules.notification.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("notification")
public class Notification {

    private Long id;

    private String receiverUuid;

    private String actorUuid;

    private String type;

    private String entityType;

    private Long entityId;

    private String bizKey;

    private Integer isRead;

    private LocalDateTime readAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}
