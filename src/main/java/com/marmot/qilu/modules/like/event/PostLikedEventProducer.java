package com.marmot.qilu.modules.like.event;

import com.marmot.qilu.modules.notification.event.PostLikedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostLikedEventProducer {

    private static final String TOPIC_INTERACTION_EVENTS = "interaction_events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPostLikedEvent(PostLikedEvent event) {
        kafkaTemplate.send(TOPIC_INTERACTION_EVENTS, event.getReceiverUuid(), event);
    }
}
