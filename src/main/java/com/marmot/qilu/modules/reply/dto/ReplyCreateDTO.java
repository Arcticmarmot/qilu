package com.marmot.qilu.modules.reply.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "创建评论回复请求")
public class ReplyCreateDTO {

    @Schema(description = "上层回复ID", example = "1")
    @NotBlank(message = "parentReplyId must not be blank")
    private Long parentReplyId;

    @Schema(description = "回复内容", example = "后会无期")
    @NotBlank(message = "content must not be blank")
    @Size(max = 1024, message = "content length must not exceed 1024")
    private String content;
}
