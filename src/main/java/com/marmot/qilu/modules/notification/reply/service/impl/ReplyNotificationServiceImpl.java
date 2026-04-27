package com.marmot.qilu.modules.notification.reply.service.impl;

import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.event.reply.ReplyEvent;
import com.marmot.qilu.modules.notification.reply.entity.ReplyNotification;
import com.marmot.qilu.modules.notification.reply.mapper.ReplyNotificationMapper;
import com.marmot.qilu.modules.notification.reply.service.ReplyNotificationService;
import com.marmot.qilu.modules.notification.reply.vo.ReplyNotificationListItemVO;
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
public class ReplyNotificationServiceImpl implements ReplyNotificationService {

    private static final int UNREAD = 0;
    private static final Duration UNREAD_COUNT_TTL = Duration.ofDays(7);

    private final ReplyNotificationMapper replyNotificationMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createReplyNotification(ReplyEvent event) {
        validateEvent(event);

        if (event.getActorUuid().equals(event.getReceiverUuid())) {
            return;
        }

        ReplyNotification notification = buildReplyNotification(event);

        try {
            int inserted = replyNotificationMapper.insert(notification);
            if (inserted != 1) {
                throw new IllegalStateException("create reply notification failed");
            }

            incrementUnreadCount(notification.getReceiverUuid());
        } catch (DuplicateKeyException e) {
            log.warn(
                    "duplicate reply notification ignored, eventId={}, replyId={}, creationType={}, creationId={}, receiverUuid={}",
                    event.getEventId(),
                    event.getReplyId(),
                    event.getCreationType(),
                    event.getCreationId(),
                    event.getReceiverUuid()
            );
        }
    }

    @Override
    public List<ReplyNotificationListItemVO> listReplyNotifications() {
        String currUserUuid = UserContext.requireUuid();

        return replyNotificationMapper.selectReplyNotifications(currUserUuid);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markReplyNotificationRead() {
        String currUserUuid = UserContext.requireUuid();

        int updated = replyNotificationMapper.updateReplyNotificationsRead(currUserUuid);

        if (updated > 0) {
            clearUnreadCount(currUserUuid);
            log.info("mark reply notifications read success, userUuid={}, updated={}", currUserUuid, updated);
        }
    }

    @Override
    public int getUnreadReplyNotificationCount() {
        String currUserUuid = UserContext.requireUuid();
        String key = buildUnreadCountKey(currUserUuid);

        String cachedValue = stringRedisTemplate.opsForValue().get(key);
        if (cachedValue != null) {
            try {
                return Integer.parseInt(cachedValue);
            } catch (NumberFormatException e) {
                log.warn("invalid cached reply unread count, userUuid={}, value={}", currUserUuid, cachedValue);
                stringRedisTemplate.delete(key);
            }
        }

        int count = replyNotificationMapper.countUnreadReplyNotifications(currUserUuid);
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
        return "notification:reply:unread:" + receiverUuid;
    }

    private void validateEvent(ReplyEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("reply event must not be null");
        }

        if (event.getEventId() == null
                || event.getActorUuid() == null
                || event.getReceiverUuid() == null
                || event.getCreationId() == null
                || event.getCreationType() == null
                || event.getReplyId() == null
                || event.getOccurredAt() == null
                || event.getContentSnippet() == null) {
            throw new IllegalArgumentException("reply event is invalid");
        }
    }

    private ReplyNotification buildReplyNotification(ReplyEvent event) {
        ReplyNotification notification = new ReplyNotification();
        notification.setReplyId(event.getReplyId());
        notification.setActorUuid(event.getActorUuid());
        notification.setReceiverUuid(event.getReceiverUuid());
        notification.setCreationId(event.getCreationId());
        notification.setCreationType(event.getCreationType().name());
        notification.setCreationSnippet(event.getCreationSnippet());
        notification.setContentSnippet(event.getContentSnippet());
        notification.setIsRead(UNREAD);
        notification.setBizKey(buildNotificationBizKey(event));
        return notification;
    }

    private String buildNotificationBizKey(ReplyEvent event) {
        return String.join(":",
                event.getReplyId().toString(),
                event.getCreationType().name(),
                event.getCreationId().toString(),
                event.getReceiverUuid()
        );
    }
}