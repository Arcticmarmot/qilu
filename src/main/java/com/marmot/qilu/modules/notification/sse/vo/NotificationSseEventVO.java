package com.marmot.qilu.modules.notification.sse.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NotificationSseEventVO {

    private String eventType;

    private String notificationType;
}
