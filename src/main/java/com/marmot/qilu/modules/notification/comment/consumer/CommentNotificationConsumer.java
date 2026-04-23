package com.marmot.qilu.modules.notification.comment.consumer;

import com.marmot.qilu.common.event.comment.CommentEvent;
import com.marmot.qilu.common.event.like.LikeEvent;
import com.marmot.qilu.modules.notification.comment.service.CommentNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import static com.marmot.qilu.common.kafka.KafkaTopics.TOPIC_COMMENT_EVENTS;

@Component
@RequiredArgsConstructor
public class CommentNotificationConsumer {

    private static final String GROUP_NOTIFICATION = "qilu-comment-notification-group";

    private final CommentNotificationService commentNotificationService;

    @KafkaListener(
            topics = TOPIC_COMMENT_EVENTS,
            groupId = GROUP_NOTIFICATION
    )
    public void onCommentEvent(CommentEvent event, Acknowledgment acknowledgement) {
        commentNotificationService.createCommentNotification(event);
        acknowledgement.acknowledge();
    }
}
