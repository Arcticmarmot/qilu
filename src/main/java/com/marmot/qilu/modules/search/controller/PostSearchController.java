package com.marmot.qilu.modules.search.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.modules.post.vo.PostPageItemVO;
import com.marmot.qilu.modules.post.vo.PostPageVO;
import com.marmot.qilu.modules.search.dto.PostSearchPageQueryDTO;
import com.marmot.qilu.modules.search.service.PostSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Search", description = "搜索索引接口")
@RestController
@RequestMapping("/search/posts")
@RequiredArgsConstructor
public class PostSearchController {

    private final PostSearchService postSearchService;

    @Operation(summary = "搜索公开帖子", description = "根据关键词搜索公开且正常状态的树状帖子，返回结果可直接用于帖子列表分页展示")
    @GetMapping
    public ApiResponse<PostPageVO<PostPageItemVO>> searchPosts(
            @Parameter(description = "搜索关键词和分页信息")
            PostSearchPageQueryDTO dto
    ) {
        return ApiResponse.success(postSearchService.searchPosts(dto));
    }
}