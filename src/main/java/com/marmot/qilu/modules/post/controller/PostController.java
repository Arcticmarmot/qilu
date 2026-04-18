package com.marmot.qilu.modules.post.controller;

import com.marmot.qilu.common.result.Result;
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
    public Result<Long> createPost(@Valid @RequestBody PostCreateDTO dto) {
        return Result.success(postService.createPost(dto));
    }

    @Operation(summary = "获取我的帖子详情", description = "返回当前登录用户自己的帖子详情，可查看自己的公开或私密帖子")
    @GetMapping("/me/{postId}")
    public Result<PostDetailVO> getMyPostDetail(@Parameter(description = "帖子ID") @PathVariable Long postId) {
        return Result.success(postService.getMyPostDetail(postId));
    }

    @Operation(summary = "获取我的帖子分页", description = "返回当前登录用户自己的帖子列表，包含公开和私密帖子")
    @GetMapping("/me")
    public Result<PostPageVO<PostPageItemVO>> getMyPostPage(PostPageQueryDTO dto) {
        return Result.success(postService.getMyPostPage(dto));
    }

    @Operation(summary = "获取公开帖子详情", description = "返回公开帖子详情，只能查看公开且正常状态的帖子")
    @GetMapping("/{postId}")
    public Result<PostDetailVO> getPublicPostDetail(@Parameter(description = "帖子ID") @PathVariable Long postId) {
        return Result.success(postService.getPublicPostDetail(postId));
    }
    @Operation(summary = "获取公开帖子分页", description = "返回公开帖子列表，只包含 status=1 且 visibility=1 的帖子")
    @GetMapping
    public Result<PostPageVO<PostPageItemVO>> getPublicPostPage(PostPageQueryDTO dto) {
        return Result.success(postService.getPublicPostPage(dto));
    }

    @Operation(summary = "更新帖子", description = "更新当前登录用户自己的帖子内容和可见性")
    @PutMapping("/{postId}")
    public Result<Void> updatePost(@Parameter(description = "帖子ID") @PathVariable Long postId, @Valid @RequestBody PostUpdateDTO dto) {
        postService.updatePost(postId, dto);
        return Result.success();
    }

    @Operation(summary = "删除帖子", description = "删除当前登录用户自己的帖子，采用软删除")
    @DeleteMapping("/{postId}")
    public Result<Void> deletePost(@Parameter(description = "帖子ID") @PathVariable Long postId) {
        postService.deletePost(postId);
        return Result.success();
    }


}
