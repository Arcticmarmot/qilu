package com.marmot.qilu.modules.notification.service;

import com.marmot.qilu.modules.notification.event.PostLikeEvent;

public interface NotificationService {

    void createPostLikeNotification(PostLikeEvent event);
}
