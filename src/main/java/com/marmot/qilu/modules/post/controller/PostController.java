package com.marmot.qilu.modules.post.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.modules.post.dto.*;
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

import java.util.List;

@Tag(name = "Post", description = "帖子相关接口：公开帖子浏览、我的帖子管理")
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @Operation(summary = "创建根帖子", description = "创建一篇新帖子，可设置为公开或仅自己可见")
    @PostMapping
    public ApiResponse<Void> createPost(@Valid @RequestBody PostCreateDTO dto) {
        postService.createPost(dto);
        return ApiResponse.success();
    }

    @Operation(summary = "创建分支帖子", description = "在指定父帖子下创建一个分支帖子，parentId 和 rootId 由后端根据父帖子自动确定")
    @PostMapping("/{parentPostId}/branches")
    public ApiResponse<Void> createBranchPost(
            @Parameter(description = "父帖子ID") @PathVariable Long parentPostId,
            @Valid @RequestBody BranchPostCreateDTO dto) {
        postService.createBranchPost(parentPostId, dto);
        return ApiResponse.success();
    }

    @Operation(summary = "更新帖子基本信息", description = "更新当前登录用户自己的帖子内容和可见性")
    @PutMapping("/{postId}")
    public ApiResponse<Void> updatePost(@Parameter(description = "根帖子ID") @PathVariable Long postId, @Valid @RequestBody PostUpdateDTO dto) {
        postService.updatePost(postId, dto);
        return ApiResponse.success();
    }

    @Operation(summary = "更新分支帖子基本信息", description = "更新当前登录用户自己的分支帖子内容和分支提示语")
    @PutMapping("/{postId}/branch")
    public ApiResponse<Void> updateBranchPost(@Parameter(description = "分支帖子ID") @PathVariable Long postId,
                                              @Valid @RequestBody BranchPostUpdateDTO dto) {
        postService.updatePostBranch(postId, dto);
        return ApiResponse.success();
    }

    @Operation(summary = "更新帖子树结构", description = "移动帖子节点到新的父节点下，或将其独立为根节点")
    @PutMapping("/{postId}/tree")
    public ApiResponse<Void> updatePostTree(
            @Parameter(description = "帖子节点ID") @PathVariable Long postId,
            @Valid @RequestBody PostTreeUpdateDTO dto) {
        postService.updatePostTree(postId, dto);
        return ApiResponse.success();
    }

    @Operation(summary = "删除帖子", description = "删除当前登录用户自己的帖子，采用软删除")
    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(@Parameter(description = "帖子ID") @PathVariable Long postId) {
        postService.deletePost(postId);
        return ApiResponse.success();
    }

    @Operation(summary = "获取我的帖子详情", description = "返回当前登录用户自己的帖子及其子帖子详情，以数组形式返回")
    @GetMapping("/me/{postId}")
    public ApiResponse<List<PostDetailVO>> getMyPostDetail(@Parameter(description = "帖子ID") @PathVariable Long postId) {
        return ApiResponse.success(postService.getMyPostDetail(postId));
    }

    @Operation(summary = "获取我的帖子分页", description = "返回当前登录用户自己的帖子列表，包含公开和私密帖子")
    @GetMapping("/me")
    public ApiResponse<PostPageVO<PostPageItemVO>> getMyPostPage(PostPageQueryDTO dto) {
        return ApiResponse.success(postService.getMyPostPage(dto));
    }

    @Operation(summary = "获取公开帖子详情", description = "返回公开帖子及其子帖子详情，以数组形式返回")
    @GetMapping("/{postId}")
    public ApiResponse<List<PostDetailVO>> getPublicPostDetail(@Parameter(description = "帖子ID") @PathVariable Long postId) {
        return ApiResponse.success(postService.getPublicPostDetail(postId));
    }
    @Operation(summary = "获取公开帖子分页", description = "返回公开帖子列表，只包含 status=1 且 visibility=1 的帖子")
    @GetMapping
    public ApiResponse<PostPageVO<PostPageItemVO>> getPublicPostPage(PostPageQueryDTO dto) {
        return ApiResponse.success(postService.getPublicPostPage(dto));
    }
}
