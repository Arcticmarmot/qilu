package com.marmot.qilu.modules.post.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PostDetailVO {

    private Long id;

    private Long userId;

    private String title;

    private String content;

    private LocalDateTime createAt;
}
