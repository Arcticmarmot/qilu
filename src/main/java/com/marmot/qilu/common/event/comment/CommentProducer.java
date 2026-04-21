package com.marmot.qilu.common.event.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.marmot.qilu.common.kafka.KafkaTopics.TOPIC_COMMENT_EVENTS;

@Component
@RequiredArgsConstructor
public class CommentProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendCommentEvent(CommentEvent event) {
        kafkaTemplate.send(TOPIC_COMMENT_EVENTS, event.getReceiverUuid(), event);
    }
}
