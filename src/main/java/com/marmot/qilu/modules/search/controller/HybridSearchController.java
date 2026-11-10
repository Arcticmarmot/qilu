package com.marmot.qilu.modules.search.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.modules.search.dto.HybridSearchQueryDTO;
import com.marmot.qilu.modules.search.service.HybridSearchService;
import com.marmot.qilu.modules.search.vo.HybridSearchItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "HybridSearch", description = "ES 与 Milvus 并行召回")
@RestController
@RequestMapping("/search/posts")
@RequiredArgsConstructor
public class HybridSearchController {

    private final HybridSearchService hybridSearchService;

    @GetMapping("/hybrid")
    @Operation(summary = "混合搜索公开帖子", description = "返回根帖卡片、命中节点、片段、召回来源和 RRF 分数")
    public ApiResponse<List<HybridSearchItemVO>> searchPosts(@Valid HybridSearchQueryDTO dto) {
        return ApiResponse.success(hybridSearchService.searchPosts(dto));
    }
}
