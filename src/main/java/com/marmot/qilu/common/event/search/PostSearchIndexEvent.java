package com.marmot.qilu.common.event.search;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class PostSearchIndexEvent {

    private String eventId;

    private PostSearchIndexAction action;

    private Long rootId;

    private List<Long> postIds;

    private String operatorUuid;

    private LocalDateTime occurredAt;
}
