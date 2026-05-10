package com.marmot.qilu.modules.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "帖子树结构更新请求")
public class PostTreeUpdateDTO {

    @Schema(description = "新的父帖子ID，为空表示将当前帖子独立为根节点", example = "1")
    private Long parentId;

    @Schema(description = "挂到父帖子下时的分支提示语，parentId 不为空时必填", example = "走向大龙山蝴蝶线")
    private String branchPrompt;
}