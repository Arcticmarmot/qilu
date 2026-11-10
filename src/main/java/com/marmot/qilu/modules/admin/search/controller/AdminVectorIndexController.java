package com.marmot.qilu.modules.admin.search.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.modules.search.service.PostVectorSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "AdminVectorIndex", description = "向量索引管理")
@RestController
@RequestMapping("/admin/search/vector-index")
@RequiredArgsConstructor
public class AdminVectorIndexController {

    private final ObjectProvider<PostVectorSearchService> postVectorSearchServiceProvider;

    @PostMapping("/rebuild")
    @Operation(summary = "回填全部帖子向量", description = "无需 Token；同步执行，返回处理节点数；公开正常节点 upsert，其余节点删除向量")
    public ApiResponse<Long> rebuildPostIndex() {
        PostVectorSearchService postVectorSearchService = postVectorSearchServiceProvider.getIfAvailable();
        if (postVectorSearchService == null) {
            throw new BadRequestException("vector indexing is disabled; set MILVUS_ENABLED=true and restart the application");
        }
        return ApiResponse.success(postVectorSearchService.rebuildPostIndex());
    }
}
