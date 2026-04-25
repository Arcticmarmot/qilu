package com.marmot.qilu.modules.reply.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.modules.reply.dto.CommentReplyCreateDTO;
import com.marmot.qilu.modules.reply.service.CommentReplyService;
import com.marmot.qilu.modules.reply.vo.CommentReplyListItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "CommentReply", description = "评论回复接口")
@RestController
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentReplyController {

    private final CommentReplyService commentReplyService;

    @Operation(summary = "创建评论回复", description = "当前登录用户对指定评论发表回复")
    @PostMapping("/{commentId}/replies")
    public ApiResponse<Void> createCommentReply(@PathVariable Long postId,
                                                @PathVariable Long commentId,
                                                @RequestBody CommentReplyCreateDTO dto) {
        commentReplyService.createCommentReply(postId, commentId, dto);
        return ApiResponse.success();
    }

    @Operation(summary = "删除评论回复", description = "当前登录用户对指定评论发表回复")
    @DeleteMapping("/{commentId}/replies/{replyId}")
    public ApiResponse<Void> deleteCommentReply(@PathVariable Long postId,
                                   @PathVariable Long commentId,
                                   @PathVariable Long replyId) {
        commentReplyService.deleteCommentReply(postId, commentId, replyId);
        return ApiResponse.success();
    }

    @Operation(summary = "获取评论回复列表", description = "获取指定评论的回复列表")
    @GetMapping("/{commentId}/replies")
    public ApiResponse<List<CommentReplyListItemVO>> listCommentReplies(@PathVariable Long postId,
                                                           @PathVariable Long commentId) {
        return ApiResponse.success(commentReplyService.listCommentReplies(postId, commentId));
    }
}