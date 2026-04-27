package com.marmot.qilu.modules.notification.like.service.impl;

import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.event.like.LikeEvent;
import com.marmot.qilu.modules.notification.like.entity.LikeNotification;
import com.marmot.qilu.modules.notification.like.mapper.LikeNotificationMapper;
import com.marmot.qilu.modules.notification.like.service.LikeNotificationService;
import com.marmot.qilu.modules.notification.like.vo.LikeNotificationListItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LikeNotificationServiceImpl implements LikeNotificationService {

    private static final int UNREAD = 0;
    private static final Duration UNREAD_COUNT_TTL = Duration.ofDays(7);

    private final LikeNotificationMapper likeNotificationMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createLikeNotification(LikeEvent event) {
        validateEvent(event);

        if (event.getActorUuid().equals(event.getReceiverUuid())) {
            return;
        }

        LikeNotification likeNotification = buildLikeNotification(event);

        try {
            int inserted = likeNotificationMapper.insert(likeNotification);
            if (inserted != 1) {
                throw new IllegalStateException("create like notification failed");
            }

            incrementUnreadCount(likeNotification.getReceiverUuid());
        } catch (DuplicateKeyException e) {
            log.warn(
                    "duplicate like notification ignored, eventId={}, creationType={}, creationId={}, actorUuid={}, receiverUuid={}",
                    event.getEventId(),
                    event.getCreationType(),
                    event.getCreationId(),
                    event.getActorUuid(),
                    event.getReceiverUuid()
            );
        }
    }

    @Override
    public List<LikeNotificationListItemVO> listLikeNotifications() {
        String currUserUuid = UserContext.requireUuid();

        return likeNotificationMapper.selectLikeNotifications(currUserUuid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markLikeNotificationsRead() {
        String currUserUuid = UserContext.requireUuid();

        int updated = likeNotificationMapper.updateLikeNotificationsRead(currUserUuid);
        if (updated > 0) {
            clearUnreadCount(currUserUuid);
            log.info("mark like notifications read success, userUuid={}, updated={}", currUserUuid, updated);
        }
    }

    @Override
    public int getUnreadLikeNotificationCount() {
        String currUserUuid = UserContext.requireUuid();
        String key = buildUnreadCountKey(currUserUuid);

        String cachedValue = stringRedisTemplate.opsForValue().get(key);
        if (cachedValue != null) {
            try {
                return Integer.parseInt(cachedValue);
            } catch (NumberFormatException e) {
                log.warn("invalid cached like unread count, userUuid={}, value={}", currUserUuid, cachedValue);
                stringRedisTemplate.delete(key);
            }
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

    private void validateEvent(LikeEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("like event must not be null");
        }

        if (event.getEventId() == null
                || event.getActorUuid() == null
                || event.getReceiverUuid() == null
                || event.getCreationId() == null
                || event.getCreationType() == null
                || event.getOccurredAt() == null) {
            throw new IllegalArgumentException("like event is invalid");
        }
    }

    private LikeNotification buildLikeNotification(LikeEvent event) {
        LikeNotification likeNotification = new LikeNotification();
        likeNotification.setReceiverUuid(event.getReceiverUuid());
        likeNotification.setActorUuid(event.getActorUuid());
        likeNotification.setCreationType(event.getCreationType().name());
        likeNotification.setCreationId(event.getCreationId());
        likeNotification.setCreationSnippet(event.getCreationSnippet());
        likeNotification.setBizKey(buildNotificationBizKey(event));
        likeNotification.setIsRead(UNREAD);
        return likeNotification;
    }

    private String buildNotificationBizKey(LikeEvent event) {
        return String.join(":",
                event.getCreationType().name(),
                event.getCreationId().toString(),
                event.getActorUuid(),
                event.getReceiverUuid()
        );
    }
}