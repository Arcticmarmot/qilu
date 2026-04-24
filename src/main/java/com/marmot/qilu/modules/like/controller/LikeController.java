package com.marmot.qilu.modules.like.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.modules.like.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Like", description = "点赞相关接口")
@RestController
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @Operation(summary = "点赞帖子", description = "当前登录用户对指定帖子点赞。重复点赞按幂等处理，不会重复增加点赞数。")
    @PostMapping("/posts/{postId}/likes")
    public ApiResponse<Void> likePost(
            @Parameter(description = "帖子ID") @PathVariable Long postId
    ) {
        likeService.likePost(postId);
        return ApiResponse.success();
    }

    @Operation(summary = "取消点赞帖子", description = "当前登录用户取消对指定帖子的点赞。重复取消按幂等处理。")
    @DeleteMapping("/posts/{postId}/likes")
    public ApiResponse<Void> unlikePost(
            @Parameter(description = "帖子ID") @PathVariable Long postId
    ) {
        likeService.unlikePost(postId);
        return ApiResponse.success();
    }

    @Operation(summary = "点赞评论", description = "当前登录用户对指定一级评论点赞。重复点赞按幂等处理，不会重复增加点赞数。")
    @PostMapping("/posts/{postId}/comments/{commentId}/likes")
    public ApiResponse<Void> likeComment(
            @Parameter(description = "帖子ID") @PathVariable Long postId,
            @Parameter(description = "评论ID") @PathVariable Long commentId
    ) {
        likeService.likeComment(postId, commentId);
        return ApiResponse.success();
    }

    @Operation(summary = "取消点赞评论", description = "当前登录用户取消对指定一级评论的点赞。重复取消按幂等处理。")
    @DeleteMapping("/posts/{postId}/comments/{commentId}/likes")
    public ApiResponse<Void> unlikeComment(
            @Parameter(description = "帖子ID") @PathVariable Long postId,
            @Parameter(description = "评论ID") @PathVariable Long commentId
    ) {
        likeService.unlikeComment(postId, commentId);
        return ApiResponse.success();
    }

    @Operation(summary = "点赞回复", description = "当前登录用户对指定回复点赞。重复点赞按幂等处理，不会重复增加点赞数。")
    @PostMapping("/posts/{postId}/comments/{commentId}/replies/{replyId}/likes")
    public ApiResponse<Void> likeReply(
            @Parameter(description = "帖子ID") @PathVariable Long postId,
            @Parameter(description = "评论ID") @PathVariable Long commentId,
            @Parameter(description = "回复ID") @PathVariable Long replyId
    ) {
        likeService.likeReply(postId, commentId, replyId);
        return ApiResponse.success();
    }

    @Operation(summary = "取消点赞回复", description = "当前登录用户取消对指定回复的点赞。重复取消按幂等处理。")
    @DeleteMapping("/posts/{postId}/comments/{commentId}/replies/{replyId}/likes")
    public ApiResponse<Void> unlikeReply(
            @Parameter(description = "帖子ID") @PathVariable Long postId,
            @Parameter(description = "评论ID") @PathVariable Long commentId,
            @Parameter(description = "回复ID") @PathVariable Long replyId
    ) {
        likeService.unlikeReply(postId, commentId, replyId);
        return ApiResponse.success();
    }
}