package com.marmot.qilu.modules.notification.comment.service.impl;

import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.event.comment.CommentEvent;
import com.marmot.qilu.modules.notification.comment.entity.CommentNotification;
import com.marmot.qilu.modules.notification.comment.mapper.CommentNotificationMapper;
import com.marmot.qilu.modules.notification.comment.service.CommentNotificationService;
import com.marmot.qilu.modules.notification.comment.vo.CommentNotificationListItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

import static com.marmot.qilu.common.util.ContentUtils.COMMENT_PREVIEW_LENGTH;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentNotificationServiceImpl implements CommentNotificationService {

    private static final int UNREAD = 0;
    private static final Duration UNREAD_COUNT_TTL = Duration.ofDays(7);

    private final CommentNotificationMapper commentNotificationMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCommentNotification(CommentEvent event) {
        validateEvent(event);

        if (event.getActorUuid().equals(event.getReceiverUuid())) {
            return;
        }

        CommentNotification notification = buildCommentNotification(event);

        try {
            int inserted = commentNotificationMapper.insert(notification);
            if (inserted != 1) {
                throw new IllegalStateException("create comment notification failed");
            }

            incrementUnreadCount(notification.getReceiverUuid());

        } catch (DuplicateKeyException e) {
            log.warn(
                    "duplicate comment notification ignored, eventId={}, commentId={}, postId={}, receiverUuid={}",
                    event.getEventId(),
                    event.getCommentId(),
                    event.getPostId(),
                    event.getReceiverUuid()
            );
        }
    }

    @Override
    public List<CommentNotificationListItemVO> listCommentNotifications() {
        String currUserUuid = UserContext.requireUuid();

        return commentNotificationMapper.selectCommentNotifications(currUserUuid, COMMENT_PREVIEW_LENGTH);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markCommentNotificationsRead() {
        String currUserUuid = UserContext.requireUuid();

        int updated = commentNotificationMapper.updateCommentNotificationsRead(currUserUuid);
        if (updated > 0) {
            clearUnreadCount(currUserUuid);
            log.info("mark comment notifications read success, userUuid={}, updated={}", currUserUuid, updated);
        }
    }

    @Override
    public int getUnreadCommentNotificationCount() {
        String currUserUuid = UserContext.requireUuid();
        String key = buildUnreadCountKey(currUserUuid);

        String cachedValue = stringRedisTemplate.opsForValue().get(key);
        if (cachedValue != null) {
            try {
                return Integer.parseInt(cachedValue);
            } catch (NumberFormatException e) {
                log.warn("invalid cached comment unread count, userUuid={}, value={}", currUserUuid, cachedValue);
                stringRedisTemplate.delete(key);
            }
        }

        int count = commentNotificationMapper.countUnreadCommentNotifications(currUserUuid);
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
        return "notification:comment:unread:" + receiverUuid;
    }

    private void validateEvent(CommentEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("comment event must not be null");
        }

        if (event.getEventId() == null
                || event.getActorUuid() == null
                || event.getReceiverUuid() == null
                || event.getPostId() == null
                || event.getCommentId() == null
                || event.getOccurredAt() == null
                || event.getContentPreview() == null) {
            throw new IllegalArgumentException("comment event is invalid");
        }
    }

    private CommentNotification buildCommentNotification(CommentEvent event) {
        CommentNotification notification = new CommentNotification();
        notification.setCommentId(event.getCommentId());
        notification.setActorUuid(event.getActorUuid());
        notification.setReceiverUuid(event.getReceiverUuid());
        notification.setPostId(event.getPostId());
        notification.setContentPreview(event.getContentPreview());
        notification.setIsRead(UNREAD);
        notification.setBizKey(buildNotificationBizKey(event));
        return notification;
    }

    private String buildNotificationBizKey(CommentEvent event) {
        return String.join(":",
                event.getCommentId().toString(),
                event.getPostId().toString(),
                event.getReceiverUuid());
    }
}