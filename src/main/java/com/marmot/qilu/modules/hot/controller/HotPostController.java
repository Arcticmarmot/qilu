package com.marmot.qilu.modules.hot.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.modules.hot.service.HotPostService;
import com.marmot.qilu.modules.post.dto.PostPageQueryDTO;
import com.marmot.qilu.modules.post.vo.PostPageItemVO;
import com.marmot.qilu.modules.post.vo.PostPageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Hot", description = "热度榜单相关接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/hot")
public class HotPostController {

    private final HotPostService hotPostService;

    @Operation(summary = "获取帖子热度榜单", description = "获取帖子热度榜单，支持分页")
    @GetMapping("/posts")
    public ApiResponse<PostPageVO<PostPageItemVO>> listHotPosts(PostPageQueryDTO dto) {
        return ApiResponse.success(hotPostService.getHotPosts(dto));
    }
}
