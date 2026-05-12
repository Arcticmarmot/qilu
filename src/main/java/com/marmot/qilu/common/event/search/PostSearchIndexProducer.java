package com.marmot.qilu.common.event.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.marmot.qilu.common.kafka.KafkaTopics.TOPIC_POST_SEARCH_INDEX_EVENTS;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostSearchIndexProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendPostSearchIndexEvent(PostSearchIndexEvent event) {
        if(event == null) {
            throw new IllegalArgumentException("post search index event must not be null");
        }
        String bizKey = String.valueOf(event.getEventId());

        kafkaTemplate.send(TOPIC_POST_SEARCH_INDEX_EVENTS, bizKey, event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error(
                                "send post search index event failed, eventId={}, rootId={}, operatorUuid={}",
                                event.getEventId(),
                                event.getRootId(),
                                event.getOperatorUuid(),
                                ex
                        );
                        return;
                    }

                    log.info(
                            "send post search index event success, eventId={}, rootId={}, operatorUuid={}",
                            event.getEventId(),
                            event.getRootId(),
                            event.getOperatorUuid()
                    );
        });

    }

}
