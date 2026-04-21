package com.marmot.qilu.modules.notification.like.service;

import com.marmot.qilu.common.event.like.LikeEvent;
import com.marmot.qilu.modules.notification.like.vo.LikeNotificationListItemVO;

import java.util.List;

public interface LikeNotificationService {

    void createLikeNotification(LikeEvent event);

    void markLikeNotificationsRead();

    List<LikeNotificationListItemVO> listLikeNotifications();

    int getUnreadLikeNotificationCount();
}
