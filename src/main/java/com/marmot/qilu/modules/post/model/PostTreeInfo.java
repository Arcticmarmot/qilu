package com.marmot.qilu.modules.post.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostTreeInfo {

    private Long id;

    private Long parentId;

    private Long rootId;

    private String userUuid;

    private Integer visibility;

    private Integer status;
}
