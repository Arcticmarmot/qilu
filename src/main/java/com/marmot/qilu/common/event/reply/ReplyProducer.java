package com.marmot.qilu.common.event.reply;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.marmot.qilu.common.kafka.KafkaTopics.TOPIC_REPLY_EVENTS;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReplyProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendReplyEvent(ReplyEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("reply event must not be null");
        }

        kafkaTemplate.send(TOPIC_REPLY_EVENTS, event.getReceiverUuid(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error(
                                "send reply event failed, eventId={}, replyId={}, entityType={}, entityId={}, actorUuid={}, receiverUuid={}",
                                event.getEventId(),
                                event.getReplyId(),
                                event.getEntityType(),
                                event.getEntityId(),
                                event.getActorUuid(),
                                event.getReceiverUuid(),
                                ex
                        );
                        return;
                    }

                    log.info(
                            "send reply event success, eventId={}, replyId={}, entityType={}, entityId={}, actorUuid={}, receiverUuid={}",
                            event.getEventId(),
                            event.getReplyId(),
                            event.getEntityType(),
                            event.getEntityId(),
                            event.getActorUuid(),
                            event.getReceiverUuid()
                    );
                });
    }
}