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
@Schema(description = "基于社区公开帖子的单轮 AI 问答")
public class RagChatRequestDTO {

    @NotBlank(message = "问题不能为空")
    @Size(max = 512, message = "问题不能超过 512 字符")
    @Schema(description = "自然语言问题", example = "Kafka 消费者失败后如何恢复？")
    private String question;

    @Min(value = 1, message = "来源数量不能小于 1")
    @Max(value = 5, message = "来源数量不能超过 5")
    @Schema(description = "最多使用的不同根帖数量，1 至 5，默认 5", example = "5")
    private int limit = 5;
}
