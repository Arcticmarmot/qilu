package com.marmot.qilu.modules.notification.service;

import com.marmot.qilu.modules.notification.event.PostLikedEvent;

public interface NotificationService {

    void createPostLikedNotification(PostLikedEvent event);
}
