package com.marmot.qilu.modules.notification.comment.consumer;

import com.marmot.qilu.common.event.comment.CommentEvent;
import com.marmot.qilu.modules.notification.comment.service.CommentNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import static com.marmot.qilu.common.kafka.KafkaTopics.TOPIC_COMMENT_EVENTS;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentNotificationConsumer {

    private static final String GROUP_NOTIFICATION = "qilu-comment-notification-group";

    private final CommentNotificationService commentNotificationService;

    @KafkaListener(
            topics = TOPIC_COMMENT_EVENTS,
            groupId = GROUP_NOTIFICATION
    )
    public void onCommentEvent(CommentEvent event, Acknowledgment acknowledgment) {
        if (event == null) {
            log.error("consume comment event failed, event is null");
            throw new IllegalArgumentException("comment event must not be null");
        }

        try {
            commentNotificationService.createCommentNotification(event);
            acknowledgment.acknowledge();

            log.debug("consume comment event success, eventId={}", event.getEventId());
        } catch (Exception e) {
            log.error(
                    "consume comment event failed, eventId={}, commentId={}, postId={}, actorUuid={}, receiverUuid={}",
                    event.getEventId(),
                    event.getCommentId(),
                    event.getPostId(),
                    event.getActorUuid(),
                    event.getReceiverUuid(),
                    e
            );
            throw e;
        }
    }
}