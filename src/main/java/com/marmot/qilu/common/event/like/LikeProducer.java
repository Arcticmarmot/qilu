package com.marmot.qilu.common.event.like;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.marmot.qilu.common.kafka.KafkaTopics.TOPIC_LIKED_EVENTS;

@Component
@RequiredArgsConstructor
public class LikeProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendLikeEvent(LikeEvent event) {
        kafkaTemplate.send(TOPIC_LIKED_EVENTS, event.getReceiverUuid(), event);
    }
}

