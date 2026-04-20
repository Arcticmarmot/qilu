package com.marmot.qilu.modules.notification.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.event.interaction.InteractionEvent;
import com.marmot.qilu.common.event.interaction.InteractionEventType;
import com.marmot.qilu.modules.notification.entity.Notification;
import com.marmot.qilu.modules.notification.mapper.NotificationMapper;
import com.marmot.qilu.modules.notification.service.NotificationService;
import com.marmot.qilu.modules.notification.vo.NotificationListItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    public void createInteractionEventNotification(InteractionEvent event) {
        if(!shouldCreateNotification(event)) {
            return;
        }

        Notification notification = buildNotification(event);

        try {
            notificationMapper.insert(notification);
        } catch (DuplicateKeyException ignored) { }
    }

    @Override
    public List<NotificationListItemVO> listNotificationsByType(InteractionEventType type) {
        String currUserUuid = UserContext.requireUuid();

        if (type == null) {
            throw new RuntimeException("Notification type must not be null.");
        }

        return notificationMapper.selectNotificationsByType(currUserUuid, type.name());
    }

    @Override
    public void markNotificationsReadByType(InteractionEventType type) {
        String currUserUuid = UserContext.requireUuid();

        if (type == null) {
            throw new RuntimeException("Notification type must not be null.");
        }

        notificationMapper.markNotificationsReadByType(currUserUuid, type.name());
    }

    private boolean shouldCreateNotification(InteractionEvent event) {
        if(event == null) return false;
        if (event.getActorUuid() == null
                || event.getReceiverUuid() == null
                || event.getEntityId() == null
                || event.getEventType() == null
                || event.getEntityType() == null) {
            return false;
        }

        return !event.getActorUuid().equals(event.getReceiverUuid());
    }


    private Notification buildNotification(InteractionEvent event) {
        Notification notification = new Notification();
        notification.setReceiverUuid(event.getReceiverUuid());
        notification.setActorUuid(event.getActorUuid());
        notification.setType(event.getEventType().name());
        notification.setEntityType(event.getEntityType().name());
        notification.setEntityId(event.getEntityId());
        notification.setBizKey(buildNotificationBizKey(event));
        notification.setIsRead(0);
        return notification;
    }

    private String buildNotificationBizKey(InteractionEvent event) {
        return event.getEventType() + ":" + event.getEntityId() + ":" +
                event.getActorUuid()+ ":" + event.getReceiverUuid() + ":" +
                event.getOccurredAt();
    }
}
