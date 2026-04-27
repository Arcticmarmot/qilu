package com.marmot.qilu.modules.notification.comment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("comment_notification")
public class CommentNotification {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long commentId;

    private String receiverUuid;

    private String actorUuid;

    private Long postId;

    private String postSnippet;

    private String contentSnippet;

    private String bizKey;

    private Integer isRead;

    private LocalDateTime readAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
