package com.marmot.qilu.modules.notification.comment.service.impl;

import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.event.comment.CommentEvent;
import com.marmot.qilu.modules.notification.comment.entity.CommentNotification;
import com.marmot.qilu.modules.notification.comment.mapper.CommentNotificationMapper;
import com.marmot.qilu.modules.notification.comment.service.CommentNotificationService;
import com.marmot.qilu.modules.notification.comment.vo.CommentNotificationListItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentNotificationServiceImpl implements CommentNotificationService {

    private static final int UNREAD = 0;
    private final CommentNotificationMapper commentNotificationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCommentNotification(CommentEvent event) {
        if(!shouldCreateNotification(event)) {
            return;
        }

        CommentNotification notification = buildCommentNotification(event);

        try {
            commentNotificationMapper.insert(notification);
        } catch (DuplicateKeyException e) {
            // 幂等重复，直接吞掉或打 info 日志
        }
    }

    @Override
    public List<CommentNotificationListItemVO> listCommentNotifications() {
        String currUserUuid = UserContext.requireUuid();

        return commentNotificationMapper.selectCommentNotifications(currUserUuid);
    }

    @Override
    public void markCommentNotificationsRead() {
        String currUserUuid = UserContext.requireUuid();

        commentNotificationMapper.updateCommentNotificationsRead(currUserUuid);
    }

    @Override
    public int getUnreadCommentNotificationCount() {
        String currUserUuid = UserContext.requireUuid();

        return commentNotificationMapper.countUnreadCommentNotifications(currUserUuid);
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
