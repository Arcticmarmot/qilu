package com.marmot.qilu.modules.notification.like.service.impl;

import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.event.like.LikeEvent;
import com.marmot.qilu.modules.notification.like.entity.LikeNotification;
import com.marmot.qilu.modules.notification.like.mapper.LikeNotificationMapper;
import com.marmot.qilu.modules.notification.like.service.LikeNotificationService;
import com.marmot.qilu.modules.notification.like.vo.LikeNotificationListItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LikeLikeNotificationServiceImpl implements LikeNotificationService {

    private static final int UNREAD = 0;
    private final LikeNotificationMapper likeNotificationMapper;

    @Override
    public void createLikeNotification(LikeEvent event) {
        if(!shouldCreateNotification(event)) {
            return;
        }

        LikeNotification likeNotification = buildLikeNotification(event);

        try {
            likeNotificationMapper.insert(likeNotification);
        } catch (DuplicateKeyException ignored) { }
    }

    @Override
    public List<LikeNotificationListItemVO> listLikeNotifications() {
        String currUserUuid = UserContext.requireUuid();

        return likeNotificationMapper.selectLikeNotifications(currUserUuid);
    }

    @Override
    public void markLikeNotificationsRead() {
        String currUserUuid = UserContext.requireUuid();

        likeNotificationMapper.markLikeNotificationsRead(currUserUuid);
    }

    private boolean shouldCreateNotification(LikeEvent event) {
        if(event == null) return false;
        if (event.getActorUuid() == null
                || event.getReceiverUuid() == null
                || event.getEntityId() == null
                || event.getEntityType() == null) {
            return false;
        }

        return !event.getActorUuid().equals(event.getReceiverUuid());
    }


    private LikeNotification buildLikeNotification(LikeEvent event) {
        LikeNotification likeNotification = new LikeNotification();
        likeNotification.setReceiverUuid(event.getReceiverUuid());
        likeNotification.setActorUuid(event.getActorUuid());
        likeNotification.setEntityType(event.getEntityType().name());
        likeNotification.setEntityId(event.getEntityId());
        likeNotification.setBizKey(buildNotificationBizKey(event));
        likeNotification.setIsRead(UNREAD);
        return likeNotification;
    }

    private String buildNotificationBizKey(LikeEvent event) {
        return String.join(":",
                event.getEntityType().name(), event.getEntityId().toString(),
                event.getActorUuid(), event.getReceiverUuid());
    }
}
