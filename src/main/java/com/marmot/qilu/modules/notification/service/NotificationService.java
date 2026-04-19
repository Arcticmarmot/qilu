package com.marmot.qilu.modules.notification.service;

import com.marmot.qilu.common.event.interaction.InteractionEvent;

public interface NotificationService {

    void createPostLikedNotification(InteractionEvent event);
}
