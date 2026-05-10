package com.marmot.qilu.modules.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BranchPostUpdateDTO {

    @Schema(description = "分支对话", example = "得到的和失去的一样多")
    @Size(max = 128, message = "title length must not exceed 128")
    private String branchPrompt;

    @Schema(description = "帖子标题", example = "挪威的森林")
    @Size(max = 128, message = "title length must not exceed 128")
    private String title;

    @Schema(description = "帖子正文", example = "全世界的老虎全部融化成黄油")
    @NotBlank(message = "content must not be blank")
    @Size(max = 4096, message = "content length must not exceed 4096")
    private String content;
}
