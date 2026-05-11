package com.marmot.qilu.modules.notification.sse.service;

import com.marmot.qilu.modules.notification.enums.NotificationType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface NotificationSseService {

    SseEmitter connect();

    void sendNotificationCreated(String receiverUuid, NotificationType notificationType);
}
