package com.marmot.qilu.modules.search.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.modules.post.vo.PostPageItemVO;
import com.marmot.qilu.modules.search.service.PostSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Search", description = "搜索索引接口")
@RestController
@RequestMapping("/search/posts")
@RequiredArgsConstructor
public class PostSearchController {

    private final PostSearchService postSearchService;

//    @Operation(summary = "同步帖子搜索索引", description = "根据帖子当前状态同步 ES 索引。公开且正常的帖子会写入索引")
//    @PostMapping("/{postId}/index")
//    public ApiResponse<Void> syncPostIndex(
//            @Parameter(description = "帖子ID", example = "101")
//            @PathVariable Long postId
//    ) {
//        postSearchService.syncPostIndex(postId);
//        return ApiResponse.success();
//    }
//
//    @Operation(summary = "删除帖子搜索索引", description = "从 ES 中删除指定帖子的搜索文档，通常用于调试、重建索引或帖子下架场景")
//    @DeleteMapping("/{postId}/index")
//    public ApiResponse<Void> deletePostIndex(
//            @Parameter(description = "帖子ID", example = "101")
//            @PathVariable Long postId
//    ) {
//        postSearchService.deletePostIndex(postId);
//        return ApiResponse.success();
//    }

    @Operation(summary = "搜索公开帖子", description = "根据关键词搜索公开且正常状态的帖子，返回结果可直接用于帖子列表展示")
    @GetMapping
    public ApiResponse<List<PostPageItemVO>> searchPosts(
            @Parameter(description = "搜索关键词，会匹配帖子标题、分支提示和正文", example = "春天")
            @RequestParam String keyword
    ) {
        return ApiResponse.success(postSearchService.searchPosts(keyword));
    }
}