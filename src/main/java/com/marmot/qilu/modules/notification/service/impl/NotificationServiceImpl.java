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
    public void createPostLikedNotification(InteractionEvent event) {
        if(event == null) {
            return;
        }

        String actorUuid = event.getActorUuid();
        String receiverUuid = event.getReceiverUuid();
        Long entityId = event.getEntityId();

        if (actorUuid == null || receiverUuid == null || entityId == null) {
            return;
        }

        if (actorUuid.equals(receiverUuid)) {
            return;
        }

        String bizKey = buildPostLikedBizKey(entityId, actorUuid, receiverUuid);

        Long count = notificationMapper.selectCount(
                Wrappers.<Notification>lambdaQuery()
                        .eq(Notification::getBizKey, bizKey)
                        .isNull(Notification::getDeletedAt)
        );

        if(count != null && count > 0) {
            return;
        }

        Notification notification = new Notification();
        notification.setReceiverUuid(receiverUuid);
        notification.setActorUuid(actorUuid);
        notification.setType(event.getEventType().name());
        notification.setEntityType(event.getEntityType().name());
        notification.setEntityId(event.getEntityId());
        notification.setBizKey(bizKey);
        notification.setIsRead(0);

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

    private String buildPostLikedBizKey(Long postId, String actorUuid, String receiverUuid) {
        return "pl" + postId + ":" + actorUuid + ":" + receiverUuid;
    }
}
