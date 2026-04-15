package com.marmot.qilu.modules.post.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostPageQueryDTO {

    private long current = 1;

    private long size = 10;
}
