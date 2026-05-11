package com.marmot.qilu.modules.notification.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    LIKE("like"),
    COMMENT("comment"),
    REPLY("reply");

    private final String value;
}
