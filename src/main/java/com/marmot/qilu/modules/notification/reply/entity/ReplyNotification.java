package com.marmot.qilu.modules.notification.reply.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("reply_notification")
public class ReplyNotification {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long replyId;

    private String receiverUuid;

    private String actorUuid;

    private String creationType;

    private Long creationId;

    private String creationSnippet;

    private String contentSnippet;

    private String bizKey;

    private Integer isRead;

    private LocalDateTime readAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
