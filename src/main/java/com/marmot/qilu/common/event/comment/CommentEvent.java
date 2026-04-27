package com.marmot.qilu.common.event.comment;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CommentEvent {

    private String eventId;

    private Long commentId;

    private String actorUuid;

    private String receiverUuid;

    private Long postId;

    private String postSnippet;

    private String contentSnippet;

    private LocalDateTime occurredAt;
}
