package com.marmot.qilu.modules.search.consumer;

import com.marmot.qilu.common.event.search.PostSearchIndexEvent;
import com.marmot.qilu.modules.search.service.PostVectorSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import static com.marmot.qilu.common.kafka.KafkaTopics.TOPIC_POST_SEARCH_INDEX_EVENTS;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "milvus", name = "enabled", havingValue = "true")
public class PostVectorIndexConsumer {

    private final PostVectorSearchService postVectorSearchService;

    @KafkaListener(id = "post-vector-index", topics = TOPIC_POST_SEARCH_INDEX_EVENTS,
            groupId = "qilu-post-vector-index-group",
            properties = "max.poll.records=1")
    public void onPostSearchIndexEvent(PostSearchIndexEvent event, Acknowledgment acknowledgment) {
        if (event == null || event.getAction() == null) {
            throw new IllegalArgumentException("post vector index event is invalid");
        }
        log.info("consume post vector index event received, eventId={}, action={}, postIds={}",
                event.getEventId(), event.getAction(), event.getPostIds());
        try {
            switch (event.getAction()) {
                case SYNC -> postVectorSearchService.syncPostIndex(event.getPostIds());
                case DELETE -> postVectorSearchService.deletePostIndex(event.getPostIds());
            }
            acknowledgment.acknowledge();
            log.info("consume post vector index event success, eventId={}, action={}, postIds={}",
                    event.getEventId(), event.getAction(), event.getPostIds());
        } catch (Exception e) {
            log.error("consume post vector index event failed, eventId={}, postIds={}",
                    event.getEventId(), event.getPostIds(), e);
            throw e;
        }
    }
}
