package com.marmot.qilu.modules.post.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PostDetailVO {

    private Long id;

    private String userUuid;

    private String nickname;

    private String title;

    private String content;

    private Integer visibility;

    private Integer likeCount;

    private Boolean likedByMe;

    private LocalDateTime createAt;
}
