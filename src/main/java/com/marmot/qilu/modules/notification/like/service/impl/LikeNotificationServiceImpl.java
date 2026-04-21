package com.marmot.qilu.modules.notification.like.service.impl;

import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.event.like.LikeEvent;
import com.marmot.qilu.modules.notification.like.entity.LikeNotification;
import com.marmot.qilu.modules.notification.like.mapper.LikeNotificationMapper;
import com.marmot.qilu.modules.notification.like.service.LikeNotificationService;
import com.marmot.qilu.modules.notification.like.vo.LikeNotificationListItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LikeNotificationServiceImpl implements LikeNotificationService {

    private static final int UNREAD = 0;
    private static final Duration UNREAD_COUNT_TTL = Duration.ofDays(7);

    private final LikeNotificationMapper likeNotificationMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void createLikeNotification(LikeEvent event) {
        if(!shouldCreateNotification(event)) {
            return;
        }

        LikeNotification likeNotification = buildLikeNotification(event);
        try {
            likeNotificationMapper.insert(likeNotification);
            incrementUnreadCount(likeNotification.getReceiverUuid());
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

        int updated = likeNotificationMapper.updateLikeNotificationsRead(currUserUuid);
        if(updated > 0) {
            clearUnreadCount(currUserUuid);
        }
    }

    @Override
    public int getUnreadLikeNotificationCount() {
        String currUserUuid = UserContext.requireUuid();
        String key = buildUnreadCountKey(currUserUuid);

        String cachedValue = stringRedisTemplate.opsForValue().get(key);
        if(cachedValue != null) {
            return Integer.parseInt(cachedValue);
        }

        int count = likeNotificationMapper.countUnreadLikeNotifications(currUserUuid);
        stringRedisTemplate.opsForValue().set(key, String.valueOf(count), UNREAD_COUNT_TTL);
        return count;
    }

    private void incrementUnreadCount(String receiverUuid) {
        String key = buildUnreadCountKey(receiverUuid);
        stringRedisTemplate.opsForValue().increment(key);
        stringRedisTemplate.expire(key, UNREAD_COUNT_TTL);
    }

    private void clearUnreadCount(String receiverUuid) {
        String key = buildUnreadCountKey(receiverUuid);
        stringRedisTemplate.delete(key);
    }

    private String buildUnreadCountKey(String receiverUuid) {
        return "notification:like:unread:" + receiverUuid;
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
