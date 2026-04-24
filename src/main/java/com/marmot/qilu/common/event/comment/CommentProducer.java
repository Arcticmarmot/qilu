package com.marmot.qilu.common.event.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.marmot.qilu.common.kafka.KafkaTopics.TOPIC_COMMENT_EVENTS;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendCommentEvent(CommentEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("comment event must not be null");
        }

        kafkaTemplate.send(TOPIC_COMMENT_EVENTS, event.getReceiverUuid(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error(
                                "send comment event failed, eventId={}, commentId={}, postId={}, receiverUuid={}",
                                event.getEventId(),
                                event.getCommentId(),
                                event.getPostId(),
                                event.getReceiverUuid(),
                                ex
                        );
                        return;
                    }

                    log.info(
                            "send comment event success, eventId={}, commentId={}, postId={}, receiverUuid={}",
                            event.getEventId(),
                            event.getCommentId(),
                            event.getPostId(),
                            event.getReceiverUuid()
                    );
                });
    }
}