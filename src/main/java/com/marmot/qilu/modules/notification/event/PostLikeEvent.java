package com.marmot.qilu.modules.notification.event;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PostLikeEvent {

    private String eventId;

    private String actorUuid;

    private String receiverUuid;

    private Long postId;

    private LocalDateTime occurredAt;
}
