package com.marmot.qilu.modules.search.consumer;

import com.marmot.qilu.common.event.search.PostSearchIndexEvent;
import com.marmot.qilu.modules.search.service.PostSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import static com.marmot.qilu.common.kafka.KafkaTopics.TOPIC_POST_SEARCH_INDEX_EVENTS;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostSearchIndexConsumer {

    private static final String GROUP_SEARCH_INDEX = "qilu-post-search-index-group";

    private final PostSearchService postSearchService;

    @KafkaListener(
            topics = TOPIC_POST_SEARCH_INDEX_EVENTS,
            groupId = GROUP_SEARCH_INDEX
    )
    public void onPostSearchIndexEvent(PostSearchIndexEvent event, Acknowledgment acknowledgment) {
        if(event == null) {
            log.error("consume post search index event failed, event is null");
            throw new IllegalArgumentException("post search index event must not be null");
        }

        try {
            switch (event.getAction()) {
                case SYNC -> postSearchService.syncPostIndex(event.getPostIds());
                case DELETE -> postSearchService.deletePostIndex(event.getPostIds());
            }

            acknowledgment.acknowledge();

            log.debug("consume post search index event success, eventId={}, action={}, postIds={}",
                    event.getEventId(), event.getAction(), event.getPostIds());
        } catch (Exception e) {
            log.error("consume post search index event failed, eventId={}, action={}, postIds={}",
                    event.getEventId(), event.getAction(), event.getPostIds(), e);
            throw e;
        }
    }
}
