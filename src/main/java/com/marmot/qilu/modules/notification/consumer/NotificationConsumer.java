package com.marmot.qilu.modules.notification.consumer;

import com.marmot.qilu.modules.notification.event.PostLikeEvent;
import com.marmot.qilu.modules.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.kafka.annotation.KafkaListener;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private static final String TOPIC_INTERACTION_EVENTS = "interaction_events";
    private static final String GROUP_NOTIFICATION = "qilu-notification-group";

    private final NotificationService notificationService;

    @KafkaListener(
            topics = TOPIC_INTERACTION_EVENTS,
            groupId = GROUP_NOTIFICATION
    )
    public void onPostLiked(PostLikeEvent event, Acknowledgment acknowledgement) {
        notificationService.createPostLikeNotification(event);
        acknowledgement.acknowledge();
    }
}
