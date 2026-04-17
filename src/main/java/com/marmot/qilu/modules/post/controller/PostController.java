package com.marmot.qilu.modules.post.controller;

import com.marmot.qilu.common.result.Result;
import com.marmot.qilu.modules.post.dto.PostCreateDTO;
import com.marmot.qilu.modules.post.dto.PostPageQueryDTO;
import com.marmot.qilu.modules.post.dto.PostUpdateDTO;
import com.marmot.qilu.modules.post.service.PostService;
import com.marmot.qilu.modules.post.vo.PostDetailVO;
import com.marmot.qilu.modules.post.vo.PostPageItemVO;
import com.marmot.qilu.modules.post.vo.PostPageVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public Result<Long> createPost(@Valid @RequestBody PostCreateDTO dto) {
        return Result.success(postService.createPost(dto));
    }

    @GetMapping("/me/{postId}")
    public Result<PostDetailVO> getMyPostDetail(@PathVariable Long postId) {
        return Result.success(postService.getMyPostDetail(postId));
    }

    @GetMapping("/me")
    public Result<PostPageVO<PostPageItemVO>> getMyPostPage(PostPageQueryDTO dto) {
        return Result.success(postService.getMyPostPage(dto));
    }

    @GetMapping("/{postId}")
    public Result<PostDetailVO> getPublicPostDetail(@PathVariable Long postId) {
        return Result.success(postService.getPublicPostDetail(postId));
    }

    @GetMapping
    public Result<PostPageVO<PostPageItemVO>> getPublicPostPage(PostPageQueryDTO dto) {
        return Result.success(postService.getPublicPostPage(dto));
    }

    @PutMapping("/{postId}")
    public Result<Void> updatePost(@PathVariable Long postId, @Valid @RequestBody PostUpdateDTO dto) {
        postService.updatePost(postId, dto);
        return Result.success();
    }

    @DeleteMapping("/{postId}")
    public Result<Void> deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return Result.success();
    }


}
