package com.marmot.qilu.common.event.reply;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.marmot.qilu.common.kafka.KafkaTopics.TOPIC_REPLY_EVENTS;

@Component
@RequiredArgsConstructor
public class ReplyProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendReplyEvent(ReplyEvent event) {
        kafkaTemplate.send(TOPIC_REPLY_EVENTS, event.getReceiverUuid(), event);
    }

}
