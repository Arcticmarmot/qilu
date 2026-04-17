package com.marmot.qilu.modules.like.controller;

import com.marmot.qilu.common.result.Result;
import com.marmot.qilu.modules.like.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping("/posts/{postId}")
    public Result<Void> likePost(@PathVariable Long postId) {
        postLikeService.likePost(postId);
        return Result.success();
    }

    @DeleteMapping("/posts/{postId}")
    public Result<Void> unlikePost(@PathVariable Long postId) {
        postLikeService.unlikePost(postId);
        return Result.success();
    }
}
