package com.marmot.qilu.modules.search.vo;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "AI 回答及本次提供给模型的真实帖子来源")
public record RagAnswerVO(String answer, List<RagReferenceVO> references) {
}
