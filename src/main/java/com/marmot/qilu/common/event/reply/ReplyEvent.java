package com.marmot.qilu.common.event.reply;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ReplyEvent {

    private String eventId;

    private Long replyId;

    private String actorUuid;

    private String receiverUuid;

    private Long creationId;

    private ReplyCreationType creationType;

    private String creationSnippet;

    private String contentSnippet;

    private LocalDateTime occurredAt;
}
