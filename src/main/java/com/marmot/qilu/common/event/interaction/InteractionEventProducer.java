package com.marmot.qilu.common.event.interaction;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.marmot.qilu.common.kafka.KafkaTopics.TOPIC_INTERACTION_EVENTS;

@Component
@RequiredArgsConstructor
public class InteractionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendInteractionEvent(InteractionEvent event) {
        kafkaTemplate.send(TOPIC_INTERACTION_EVENTS, event.getReceiverUuid(), event);
    }
}

