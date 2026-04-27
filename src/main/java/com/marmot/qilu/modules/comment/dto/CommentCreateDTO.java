package com.marmot.qilu.modules.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "创建帖子评论请求")
public class CommentCreateDTO {

    @Schema(description = "评论内容", example = "后会无期")
    @NotBlank(message = "content must not be blank")
    @Size(max = 1024, message = "content length must not exceed 1024")
    private String content;
}
