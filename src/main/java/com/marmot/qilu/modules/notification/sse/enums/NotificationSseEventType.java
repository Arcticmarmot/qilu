package com.marmot.qilu.modules.notification.sse.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationSseEventType {

    CONNECTED("connected"),
    NOTIFICATION_CREATED("notification_created");

    private final String value;

}
