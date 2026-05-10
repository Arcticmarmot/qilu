package com.marmot.qilu.modules.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BranchPostCreateDTO {

    @Schema(description = "分支对话", example = "得到的和失去的一样多")
    @Size(max = 128, message = "title length must not exceed 128")
    private String branchPrompt;

    @Schema(description = "帖子标题", example = "经营未来")
    @Size(max = 128, message = "title length must not exceed 128")
    private String title;

    @Schema(description = "帖子正文", example = "在所有失去的人中，我最怀念我自己")
    @NotBlank(message = "content must not be blank")
    @Size(max = 4096, message = "content length must not exceed 4096")
    private String content;

    @Schema(description = "媒体id组")
    @Size(max = 10, message = "media count not exceed 9")
    private List<Long> mediaIds;
}
