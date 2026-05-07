package com.marmot.qilu.modules.hot.consumer;

import com.marmot.qilu.common.event.like.LikeEvent;
import com.marmot.qilu.modules.hot.service.HotPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import static com.marmot.qilu.common.kafka.KafkaTopics.TOPIC_LIKE_EVENTS;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeHotConsumer {

    private static final String GROUP_HOT = "qilu-like-hot-group";

    private final HotPostService hotPostService;

    @KafkaListener(
            topics = TOPIC_LIKE_EVENTS,
            groupId = GROUP_HOT
    )
    public void onLikeEvent(LikeEvent event, Acknowledgment acknowledgment) {
        if (event == null) {
            log.error("consume like hot event failed, event is null");
            throw new IllegalArgumentException("like event must not be null");
        }

        try {
            hotPostService.increaseByLike(event);
            acknowledgment.acknowledge();

            log.debug("consume like hot event success, eventId={}", event.getEventId());
        } catch (Exception e) {
            log.error(
                    "consume like hot event failed, eventId={}, creationType={}, creationId={}, actorUuid={}, receiverUuid={}",
                    event.getEventId(),
                    event.getCreationType(),
                    event.getCreationId(),
                    event.getActorUuid(),
                    event.getReceiverUuid(),
                    e
            );
            throw e;
        }
    }
}
