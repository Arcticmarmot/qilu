package com.marmot.qilu.modules.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserDTO {
    private String nickname;

    private String email;

    private String password;
}
