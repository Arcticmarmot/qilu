package com.marmot.qilu.modules.notification.reply.service;

import com.marmot.qilu.common.event.reply.ReplyEvent;
import com.marmot.qilu.modules.notification.reply.vo.ReplyNotificationListItemVO;

import java.util.List;

public interface ReplyNotificationService {

    void createReplyNotification(ReplyEvent event);

    List<ReplyNotificationListItemVO> listReplyNotifications();

    void markReplyNotificationRead();

    int getUnreadReplyNotificationCount();
}
