package com.marmot.qilu.modules.post.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PostPageVO<T> {

    private long current;

    private long size;

    private long total;

    private List<T> records;
}
