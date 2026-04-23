package com.marmot.qilu.modules.notification.reply.consumer;

import com.marmot.qilu.common.event.reply.ReplyEvent;
import com.marmot.qilu.modules.notification.reply.service.ReplyNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import static com.marmot.qilu.common.kafka.KafkaTopics.TOPIC_REPLY_EVENTS;

@Component
@RequiredArgsConstructor
public class ReplyNotificationConsumer {

    private static final String GROUP_NOTIFICATION = "qilu-reply-notification-group";

    private final ReplyNotificationService replyNotificationService;

    @KafkaListener(
            topics = TOPIC_REPLY_EVENTS,
            groupId = GROUP_NOTIFICATION
    )
    public void sendReplyEvent(ReplyEvent event, Acknowledgment acknowledgment) {
        replyNotificationService.createReplyNotification(event);
        acknowledgment.acknowledge();
    }

}
