package com.marmot.qilu.modules.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "搜索帖子分页查询参数")
public class PostSearchPageQueryDTO {

    @Schema(description = "关键词", example = "猫")
    private String keyword;

    @Schema(description = "页码，从 1 开始", example = "1")
    @Min(value = 1, message = "current page must be >= 1")
    private int current = 1;

    @Schema(description = "每页条数", example = "10")
    @Min(value = 1, message = "page size must be >= 1")
    private int size = 10;
}
