package com.marmot.qilu.modules.hot.consumer;

import com.marmot.qilu.common.event.comment.CommentEvent;
import com.marmot.qilu.modules.hot.service.HotPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import static com.marmot.qilu.common.kafka.KafkaTopics.TOPIC_COMMENT_EVENTS;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommentHotConsumer {

    private static final String GROUP_HOT = "qilu-comment-hot-group";

    private HotPostService hotPostService;

    @KafkaListener(
            topics = TOPIC_COMMENT_EVENTS,
            groupId = GROUP_HOT
    )
    public void onCommentEvent(CommentEvent event, Acknowledgment acknowledgment) {
        if (event == null) {
            log.error("consume comment hot event failed, event is null");
            throw new IllegalArgumentException("comment event must not be null");
        }

        try {
            hotPostService.increaseByComment(event);
            acknowledgment.acknowledge();

            log.debug("consume comment hot event success, eventId={}", event.getEventId());
        } catch (Exception e) {
            log.error(
                    "consume comment hot event failed, eventId={}, commentId={}, postId={}, actorUuid={}, receiverUuid={}",
                    event.getEventId(),
                    event.getCommentId(),
                    event.getPostId(),
                    event.getActorUuid(),
                    event.getReceiverUuid(),
                    e
            );
            throw e;
        }
    }
}
