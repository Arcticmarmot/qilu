package com.marmot.qilu.modules.notification.service;

import com.marmot.qilu.common.event.interaction.InteractionEntityType;
import com.marmot.qilu.common.event.interaction.InteractionEvent;
import com.marmot.qilu.common.event.interaction.InteractionEventType;
import com.marmot.qilu.modules.notification.vo.NotificationListItemVO;

import java.util.List;

public interface NotificationService {

    void createPostLikedNotification(InteractionEvent event);

    List<NotificationListItemVO> listNotificationsByType(InteractionEventType type);

    void markNotificationsReadByType(InteractionEventType type);
}
