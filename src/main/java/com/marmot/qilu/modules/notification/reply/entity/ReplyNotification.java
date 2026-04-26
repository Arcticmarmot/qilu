package com.marmot.qilu.modules.notification.reply.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("reply_notification")
public class ReplyNotification {

    private Long id;

    private Long replyId;

    private String receiverUuid;

    private String actorUuid;

    private String entityType;

    private Long entityId;

    private String entitySnippet;

    private String contentPreview;

    private String bizKey;

    private Integer isRead;

    private LocalDateTime readAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
