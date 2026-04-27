package com.marmot.qilu.modules.notification.like.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("like_notification")
public class LikeNotification {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String receiverUuid;

    private String actorUuid;

    private String creationType;

    private Long creationId;

    private String creationSnippet;

    private String bizKey;

    private Integer isRead;

    private LocalDateTime readAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
