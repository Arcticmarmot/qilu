package com.marmot.qilu.modules.notification.comment.service.impl;

import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.event.comment.CommentEvent;
import com.marmot.qilu.modules.notification.comment.entity.CommentNotification;
import com.marmot.qilu.modules.notification.comment.mapper.CommentNotificationMapper;
import com.marmot.qilu.modules.notification.comment.service.CommentNotificationService;
import com.marmot.qilu.modules.notification.comment.vo.CommentNotificationListItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

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
        if(!shouldCreateNotification(event)) {
            return;
        }

        CommentNotification notification = buildCommentNotification(event);

        try {
            commentNotificationMapper.insert(notification);
            incrementUnreadCount(notification.getReceiverUuid());
        } catch (DuplicateKeyException ignored) { }
    }

    @Override
    public List<CommentNotificationListItemVO> listCommentNotifications() {
        String currUserUuid = UserContext.requireUuid();

        return commentNotificationMapper.selectCommentNotifications(currUserUuid);
    }

    @Override
    public void markCommentNotificationsRead() {
        String currUserUuid = UserContext.requireUuid();

        int updated = commentNotificationMapper.updateCommentNotificationsRead(currUserUuid);
        if(updated > 0) {
            clearUnreadCount(currUserUuid);
        }
    }

    @Override
    public int getUnreadCommentNotificationCount() {
        String currUserUuid = UserContext.requireUuid();
        String key = buildUnreadCountKey(currUserUuid);

        String cachedValue = stringRedisTemplate.opsForValue().get(key);
        if(cachedValue != null) {
            return Integer.parseInt(cachedValue);
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

    private boolean shouldCreateNotification(CommentEvent event) {
        if(event == null) return false;
        if (event.getActorUuid() == null
                || event.getContentPreview() == null
                || event.getReceiverUuid() == null
                || event.getEntityId() == null
                || event.getEntityType() == null) {
            return false;
        }

        return !event.getActorUuid().equals(event.getReceiverUuid());
    }

    private CommentNotification buildCommentNotification(CommentEvent event) {
        CommentNotification notification = new CommentNotification();
        notification.setCommentId(event.getCommentId());
        notification.setActorUuid(event.getActorUuid());
        notification.setReceiverUuid(event.getReceiverUuid());
        notification.setEntityId(event.getEntityId());
        notification.setEntityType(event.getEntityType().name());
        notification.setContentPreview(event.getContentPreview());
        notification.setIsRead(UNREAD);
        notification.setBizKey(buildNotificationBizKey(event));
        return notification;
    }

    private String buildNotificationBizKey(CommentEvent event) {
        return String.join(":",
                event.getCommentId().toString(), event.getEntityType().name(),
                event.getEntityId().toString(), event.getReceiverUuid());
    }
}
