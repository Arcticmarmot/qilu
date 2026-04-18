package com.marmot.qilu.modules.notification.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.marmot.qilu.modules.notification.entity.Notification;
import com.marmot.qilu.modules.notification.event.PostLikeEvent;
import com.marmot.qilu.modules.notification.mapper.NotificationMapper;
import com.marmot.qilu.modules.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.sql.Wrapper;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    public void createPostLikeNotification(PostLikeEvent event) {
        if(event == null) {
            return;
        }

        String actorUuid = event.getActorUuid();
        String receiverUuid = event.getReceiverUuid();
        Long postId = event.getPostId();

        if (actorUuid == null || receiverUuid == null || postId == null) {
            return;
        }

        if (actorUuid.equals(receiverUuid)) {
            return;
        }

        String bizKey = buildPostLikedBizKey(postId, actorUuid, receiverUuid);

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
        notification.setType("POST_LIKED");
        notification.setEntityType("POST");
        notification.setEntityId(postId);
        notification.setBizKey(bizKey);
        notification.setIsRead(0);

        try {
            notificationMapper.insert(notification);
        } catch (DuplicateKeyException ignored) { }

    }

    private String buildPostLikedBizKey(Long postId, String actorUuid, String receiverUuid) {
        return "pl" + postId + ":" + actorUuid + ":" + receiverUuid;
    }
}
