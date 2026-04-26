package com.marmot.qilu.common.event.like;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LikeEvent {

    private String eventId;

    private String actorUuid;

    private String receiverUuid;

    private Long entityId;

    private LikeEntityType entityType;

    private String entitySnippet;

    private LocalDateTime occurredAt;
}
