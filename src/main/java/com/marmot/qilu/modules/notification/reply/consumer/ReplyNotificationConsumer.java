package com.marmot.qilu.modules.notification.reply.consumer;

import com.marmot.qilu.common.event.reply.ReplyEvent;
import com.marmot.qilu.modules.notification.reply.service.ReplyNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import static com.marmot.qilu.common.kafka.KafkaTopics.TOPIC_REPLY_EVENTS;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReplyNotificationConsumer {

    private static final String GROUP_NOTIFICATION = "qilu-reply-notification-group";

    private final ReplyNotificationService replyNotificationService;

    @KafkaListener(
            topics = TOPIC_REPLY_EVENTS,
            groupId = GROUP_NOTIFICATION
    )
    public void onReplyEvent(ReplyEvent event, Acknowledgment acknowledgment) {
        if (event == null) {
            log.error("consume reply event failed, event is null");
            throw new IllegalArgumentException("reply event must not be null");
        }

        try {
            replyNotificationService.createReplyNotification(event);
            acknowledgment.acknowledge();

            log.debug("consume reply event success, eventId={}", event.getEventId());
        } catch (Exception e) {
            log.error(
                    "consume reply event failed, eventId={}, replyId={}, entityType={}, entityId={}, actorUuid={}, receiverUuid={}",
                    event.getEventId(),
                    event.getReplyId(),
                    event.getEntityType(),
                    event.getEntityId(),
                    event.getActorUuid(),
                    event.getReceiverUuid(),
                    e
            );
            throw e;
        }
    }
}