package com.marmot.qilu.modules.notification.like.consumer;

import com.marmot.qilu.common.event.like.LikeEvent;
import com.marmot.qilu.modules.notification.like.service.LikeNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.kafka.annotation.KafkaListener;
import static com.marmot.qilu.common.kafka.KafkaTopics.TOPIC_LIKE_EVENTS;

@Component
@RequiredArgsConstructor
public class LikeNotificationConsumer {

    private static final String GROUP_NOTIFICATION = "qilu-like-notification-group";

    private final LikeNotificationService likeNotificationService;

    @KafkaListener(
            topics = TOPIC_LIKE_EVENTS,
            groupId = GROUP_NOTIFICATION
    )
    public void onLikeEvent(LikeEvent event, Acknowledgment acknowledgement) {
        likeNotificationService.createLikeNotification(event);
        acknowledgement.acknowledge();
    }
}
