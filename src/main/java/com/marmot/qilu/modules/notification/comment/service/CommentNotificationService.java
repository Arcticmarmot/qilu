package com.marmot.qilu.modules.notification.comment.service;

import com.marmot.qilu.common.event.comment.CommentEvent;
import com.marmot.qilu.modules.notification.comment.vo.CommentNotificationListItemVO;

import java.util.List;

public interface CommentNotificationService {

    void createCommentNotification(CommentEvent event);

    List<CommentNotificationListItemVO> listCommentNotifications();

    void markCommentNotificationsRead();

    int getUnreadCommentNotificationCount();
}
