package com.marmot.qilu.modules.post.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostUpdateDTO {

    @Size(max = 128, message = "title length must not exceed 128")
    private String title;

    @NotBlank(message = "content must not be blank")
    @Size(max = 4096, message = "content length must not exceed 4096")
    private String content;

    @NotNull(message = "visibility must not be null")
    @Min(value = 1, message = "visibility must be 1 or 2")
    @Max(value = 2, message = "visibility must be 1 or 2")
    private Integer visibility;
}