package com.marmot.qilu.modules.post.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PostPageItemVO {

    private Long id;

    private String userUuid;

    private String nickname;

    private String title;

    private String contentPreview;

    private LocalDateTime createdAt;
}
