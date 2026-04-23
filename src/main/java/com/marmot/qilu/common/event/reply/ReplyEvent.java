package com.marmot.qilu.common.event.reply;

import com.marmot.qilu.common.event.like.LikeEntityType;
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

    private Long entityId;

    private ReplyEntityType entityType;

    private String contentPreview;

    private LocalDateTime occurredAt;
}
