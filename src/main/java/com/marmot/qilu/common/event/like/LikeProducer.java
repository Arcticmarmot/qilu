package com.marmot.qilu.common.event.like;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.marmot.qilu.common.kafka.KafkaTopics.TOPIC_LIKE_EVENTS;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendLikeEvent(LikeEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("like event must not be null");
        }

        kafkaTemplate.send(TOPIC_LIKE_EVENTS, event.getReceiverUuid(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error(
                                "send like event failed, eventId={}, entityType={}, entityId={}, actorUuid={}, receiverUuid={}",
                                event.getEventId(),
                                event.getEntityType(),
                                event.getEntityId(),
                                event.getActorUuid(),
                                event.getReceiverUuid(),
                                ex
                        );
                        return;
                    }

                    log.info(
                            "send like event success, eventId={}, entityType={}, entityId={}, actorUuid={}, receiverUuid={}",
                            event.getEventId(),
                            event.getEntityType(),
                            event.getEntityId(),
                            event.getActorUuid(),
                            event.getReceiverUuid()
                    );
                });
    }
}