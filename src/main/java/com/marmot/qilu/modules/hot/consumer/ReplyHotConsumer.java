package com.marmot.qilu.modules.hot.consumer;

import com.marmot.qilu.common.event.reply.ReplyEvent;
import com.marmot.qilu.modules.hot.service.HotPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import static com.marmot.qilu.common.kafka.KafkaTopics.TOPIC_REPLY_EVENTS;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReplyHotConsumer {

    private static final String GROUP_HOT = "qilu-reply-hot-group";

    private final HotPostService hotPostService;

    @KafkaListener(
            topics = TOPIC_REPLY_EVENTS,
            groupId = GROUP_HOT
    )
    public void onReplyEvent(ReplyEvent event, Acknowledgment acknowledgment) {
        if (event == null) {
            log.error("consume reply hot event failed, event is null");
            throw new IllegalArgumentException("reply event must not be null");
        }

        try {
            hotPostService.increaseByReply(event);
            acknowledgment.acknowledge();

            log.debug("consume reply hot event success, eventId={}", event.getEventId());
        } catch (Exception e) {
            log.error(
                    "consume reply hot event failed, eventId={}, replyId={}, creationType={}, creationId={}, actorUuid={}, receiverUuid={}",
                    event.getEventId(),
                    event.getReplyId(),
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
