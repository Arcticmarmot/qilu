package com.marmot.qilu.modules.comment.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.modules.comment.dto.CommentCreateDTO;
import com.marmot.qilu.modules.comment.service.CommentService;
import com.marmot.qilu.modules.comment.vo.CommentListItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Comment", description = "帖子评论接口")
@RestController
@RequestMapping("/posts/{postId}")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "创建帖子评论", description = "当前登录用户对指定帖子发表评论")
    @PostMapping("/comments")
    public ApiResponse<Long> createPostComment(
            @Parameter(description = "帖子ID", example = "1", required = true)
            @PathVariable Long postId,
            @Parameter(description = "评论创建输入")
            @Valid @RequestBody CommentCreateDTO dto
    ) {
        return ApiResponse.success(commentService.createComment(postId, dto));
    }

    @Operation(summary = "删除自己的评论", description = "当前登录用户删除自己发布的一级评论")
    @DeleteMapping("/comments/{commentId}")
    public ApiResponse<Void> deletePostComment(
            @Parameter(description = "帖子ID", example = "1", required = true)
            @PathVariable Long postId,
            @Parameter(description = "评论ID", example = "1", required = true)
            @PathVariable Long commentId
    ) {
        commentService.deleteComment(postId, commentId);
        return ApiResponse.success();
    }

    @Operation(summary = "获取帖子评论列表", description = "获取指定帖子的一级评论列表")
    @GetMapping("/comments")
    public ApiResponse<List<CommentListItemVO>> listPostComments(
            @Parameter(description = "帖子ID", example = "1", required = true)
            @PathVariable Long postId) {
        return ApiResponse.success(commentService.listComments(postId));
    }
}
