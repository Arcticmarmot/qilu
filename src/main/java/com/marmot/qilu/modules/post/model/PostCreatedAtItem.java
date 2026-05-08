package com.marmot.qilu.modules.post.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PostCreatedAtItem {

    private Long id;

    private LocalDateTime createdAt;
}
