package com.marmot.qilu.modules.hot.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.modules.hot.service.HotPostService;
import com.marmot.qilu.modules.post.dto.PostPageQueryDTO;
import com.marmot.qilu.modules.post.vo.PostPageItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/hot")
public class HotPostController {

    private final HotPostService hotPostService;

    @GetMapping("/posts")
    public ApiResponse<List<PostPageItemVO>> listHotPosts(
            @RequestParam(defaultValue = "DAILY") String range, PostPageQueryDTO dto
    ) {
        return ApiResponse.success(hotPostService.getHotPosts(range, dto));
    }
}
