package com.marmot.qilu.modules.post.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PostSearchSource {

    private Long id;

    private Long rootId;

    private String userUuid;

    private String title;

    private String content;

    private String branchPrompt;

    private Integer status;

    private Integer visibility;

    private LocalDateTime createdAt;
}
