package com.marmot.qilu.modules.auth.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginVO {
    private String token;

    private String uuid;

    private String nickname;

    private String email;
}
