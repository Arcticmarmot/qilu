package com.marmot.qilu.modules.like.controller;

import com.marmot.qilu.common.result.Result;
import com.marmot.qilu.modules.like.service.PostLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Post", description = "帖子点赞相关接口")
@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    @Operation(summary = "点赞帖子", description = "当前登录用户对指定帖子点赞。重复点赞按幂等处理，不会重复增加点赞数。")
    @PostMapping("/posts/{postId}")
    public Result<Void> likePost(@Parameter(description = "帖子ID") @PathVariable Long postId) {
        postLikeService.likePost(postId);
        return Result.success();
    }

    @Operation(summary = "取消点赞", description = "当前登录用户取消对指定帖子的点赞。重复取消按幂等处理。")
    @DeleteMapping("/posts/{postId}")
    public Result<Void> unlikePost(@Parameter(description = "帖子ID") @PathVariable Long postId) {
        postLikeService.unlikePost(postId);
        return Result.success();
    }
}
