package com.marmot.qilu.modules.user.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserVO {
    private String uuid;

    private String nickname;

    private String email;

    private Integer status;

    private LocalDateTime createAt;
}
