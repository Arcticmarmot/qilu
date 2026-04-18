package com.marmot.qilu.modules.notification.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class NotificationListItemVO {

    private Long id;

    private String actorUuid;

    private String type;

    private String entityType;

    private Long entityId;

    private Integer isRead;

    private LocalDateTime createAt;
}
