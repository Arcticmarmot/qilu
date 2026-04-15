package com.marmot.qilu.modules.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostCreateDTO {

    @Size(max = 128, message = "title length must not exceed 128")
    private String title;

    @NotBlank(message = "content must not be blank")
    @Size(max = 128, message = "content length must not exceed 128")
    private String content;
}
