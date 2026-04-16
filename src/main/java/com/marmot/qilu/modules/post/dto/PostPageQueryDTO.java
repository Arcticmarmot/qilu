package com.marmot.qilu.modules.post.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostPageQueryDTO {

    @Min(value = 1, message = "current page must be >= 1")
    private long current = 1;

    @Min(value = 1, message = "page size must be >= 1")
    private long size = 10;
}
