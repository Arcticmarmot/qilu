package com.marmot.qilu.modules.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "创建帖子请求")
public class PostCreateDTO {

    @Schema(description = "帖子标题", example = "经营未来")
    @Size(max = 128, message = "title length must not exceed 128")
    private String title;

    @Schema(description = "帖子正文", example = "在所有失去的人中，我最怀念我自己")
    @NotBlank(message = "content must not be blank")
    @Size(max = 4096, message = "content length must not exceed 4096")
    private String content;

    @Schema(description = "可见性：1公开 2仅自己", example = "1")
    @NotNull(message = "visibility must not be null")
    @Min(value = 1, message = "visibility must be 1 or 2")
    @Max(value = 2, message = "visibility must be 1 or 2")
    private Integer visibility;
}
