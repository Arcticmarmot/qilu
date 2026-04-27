package com.marmot.qilu.modules.notification.like.consumer;

import com.marmot.qilu.common.event.like.LikeEvent;
import com.marmot.qilu.modules.notification.like.service.LikeNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import static com.marmot.qilu.common.kafka.KafkaTopics.TOPIC_LIKE_EVENTS;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeNotificationConsumer {

    private static final String GROUP_NOTIFICATION = "qilu-like-notification-group";

    private final LikeNotificationService likeNotificationService;

    @KafkaListener(
            topics = TOPIC_LIKE_EVENTS,
            groupId = GROUP_NOTIFICATION
    )
    public void onLikeEvent(LikeEvent event, Acknowledgment acknowledgment) {
        if (event == null) {
            log.error("consume like event failed, event is null");
            throw new IllegalArgumentException("like event must not be null");
        }

        try {
            likeNotificationService.createLikeNotification(event);
            acknowledgment.acknowledge();

            log.debug("consume like event success, eventId={}", event.getEventId());
        } catch (Exception e) {
            log.error(
                    "consume like event failed, eventId={}, creationType={}, creationId={}, actorUuid={}, receiverUuid={}",
                    event.getEventId(),
                    event.getCreationType(),
                    event.getCreationId(),
                    event.getActorUuid(),
                    event.getReceiverUuid(),
                    e
            );
            throw e;
        }
    }
}