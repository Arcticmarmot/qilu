package com.marmot.qilu.modules.post.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.modules.post.dto.PostCreateDTO;
import com.marmot.qilu.modules.post.dto.PostPageQueryDTO;
import com.marmot.qilu.modules.post.dto.PostUpdateDTO;
import com.marmot.qilu.modules.post.service.PostService;
import com.marmot.qilu.modules.post.vo.PostDetailVO;
import com.marmot.qilu.modules.post.vo.PostPageItemVO;
import com.marmot.qilu.modules.post.vo.PostPageVO;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

@Tag(name = "Post", description = "帖子相关接口：公开帖子浏览、我的帖子管理")
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "创建帖子", description = "创建一篇新帖子，可设置为公开或仅自己可见")
    @PostMapping
    public ApiResponse<Long> createPost(@Valid @RequestBody PostCreateDTO dto) {
        postService.createPost(dto);
        return ApiResponse.success();
    }

    @Operation(summary = "获取我的帖子详情", description = "返回当前登录用户自己的帖子详情，可查看自己的公开或私密帖子")
    @GetMapping("/me/{postId}")
    public ApiResponse<PostDetailVO> getMyPostDetail(@Parameter(description = "帖子ID") @PathVariable Long postId) {
        return ApiResponse.success(postService.getMyPostDetail(postId));
    }

    @Operation(summary = "获取我的帖子分页", description = "返回当前登录用户自己的帖子列表，包含公开和私密帖子")
    @GetMapping("/me")
    public ApiResponse<PostPageVO<PostPageItemVO>> getMyPostPage(PostPageQueryDTO dto) {
        return ApiResponse.success(postService.getMyPostPage(dto));
    }

    @Operation(summary = "获取公开帖子详情", description = "返回公开帖子详情，只能查看公开且正常状态的帖子")
    @GetMapping("/{postId}")
    public ApiResponse<PostDetailVO> getPublicPostDetail(@Parameter(description = "帖子ID") @PathVariable Long postId) {
        return ApiResponse.success(postService.getPublicPostDetail(postId));
    }
    @Operation(summary = "获取公开帖子分页", description = "返回公开帖子列表，只包含 status=1 且 visibility=1 的帖子")
    @GetMapping
    public ApiResponse<PostPageVO<PostPageItemVO>> getPublicPostPage(PostPageQueryDTO dto) {
        return ApiResponse.success(postService.getPublicPostPage(dto));
    }

    @Operation(summary = "更新帖子", description = "更新当前登录用户自己的帖子内容和可见性")
    @PutMapping("/{postId}")
    public ApiResponse<Void> updatePost(@Parameter(description = "帖子ID") @PathVariable Long postId, @Valid @RequestBody PostUpdateDTO dto) {
        postService.updatePost(postId, dto);
        return ApiResponse.success();
    }

    @Operation(summary = "删除帖子", description = "删除当前登录用户自己的帖子，采用软删除")
    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(@Parameter(description = "帖子ID") @PathVariable Long postId) {
        postService.deletePost(postId);
        return ApiResponse.success();
    }


}
