package com.marmot.qilu.common.event.interaction;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class InteractionEvent {

    private String eventId;

    private String actorUuid;

    private String receiverUuid;

    private InteractionEventType eventType;

    private Long entityId;

    private InteractionEntityType entityType;

    private LocalDateTime occurredAt;
}
