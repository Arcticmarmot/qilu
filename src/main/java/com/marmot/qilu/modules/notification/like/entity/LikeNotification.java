package com.marmot.qilu.modules.notification.like.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("like_notification")
public class LikeNotification {

    private Long id;

    private String receiverUuid;

    private String actorUuid;

    private String entityType;

    private Long entityId;

    private String entitySnippet;

    private String bizKey;

    private Integer isRead;

    private LocalDateTime readAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
