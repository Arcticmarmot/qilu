package com.marmot.qilu.modules.notification.consumer;

import com.marmot.qilu.common.event.interaction.InteractionEvent;
import com.marmot.qilu.modules.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.kafka.annotation.KafkaListener;
import static com.marmot.qilu.common.kafka.KafkaTopics.TOPIC_INTERACTION_EVENTS;

@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private static final String GROUP_NOTIFICATION = "qilu-notification-group";

    private final NotificationService notificationService;

    @KafkaListener(
            topics = TOPIC_INTERACTION_EVENTS,
            groupId = GROUP_NOTIFICATION
    )
    public void onInteractionEvent(InteractionEvent event, Acknowledgment acknowledgement) {
        notificationService.createInteractionEventNotification(event);
        acknowledgement.acknowledge();
    }
}
