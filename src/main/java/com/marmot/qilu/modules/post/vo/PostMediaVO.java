package com.marmot.qilu.modules.post.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostMediaVO {

    private Long postId;

    private Long mediaId;

    private String url;

    private Integer sortOrder;
}
