package com.marmot.qilu.modules.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "混合搜索参数，MVP 返回 Top K，不提供深分页")
public class HybridSearchQueryDTO {

    @NotBlank
    @Size(max = 512)
    @Schema(description = "关键词或自然语言问题", example = "如何避免 Kafka 消息重复消费")
    private String keyword;

    @Min(1)
    @Max(50)
    @Schema(description = "返回条数，1 至 50", example = "10")
    private int limit = 10;
}
