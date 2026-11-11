package com.marmot.qilu.modules.search.vo;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "来自 MySQL 的真实帖子来源，数组第 n 项对应回答中的 [n]")
public record RagReferenceVO(
        Long postId,
        Long matchedNodeId,
        String title,
        String url
) {
}
