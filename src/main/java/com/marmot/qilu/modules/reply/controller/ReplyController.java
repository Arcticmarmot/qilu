package com.marmot.qilu.modules.reply.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.modules.reply.dto.ReplyCreateDTO;
import com.marmot.qilu.modules.reply.service.ReplyService;
import com.marmot.qilu.modules.reply.vo.ReplyListItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Reply", description = "评论回复接口")
@RestController
@RequestMapping("/posts/{postId}/comments")
@RequiredArgsConstructor
public class ReplyController {

    private final ReplyService replyService;

    @Operation(summary = "创建评论回复", description = "当前登录用户对指定评论发表回复")
    @PostMapping("/{commentId}/replies")
    public ApiResponse<Void> createCommentReply(@PathVariable Long postId,
                                                @PathVariable Long commentId,
                                                @RequestBody ReplyCreateDTO dto) {
        replyService.createReply(postId, commentId, dto);
        return ApiResponse.success();
    }

    @Operation(summary = "删除评论回复", description = "当前登录用户对指定评论发表回复")
    @DeleteMapping("/{commentId}/replies/{replyId}")
    public ApiResponse<Void> deleteCommentReply(@PathVariable Long postId,
                                   @PathVariable Long commentId,
                                   @PathVariable Long replyId) {
        replyService.deleteReply(postId, commentId, replyId);
        return ApiResponse.success();
    }

    @Operation(summary = "获取评论回复列表", description = "获取指定评论的回复列表")
    @GetMapping("/{commentId}/replies")
    public ApiResponse<List<ReplyListItemVO>> listCommentReplies(@PathVariable Long postId,
                                                                 @PathVariable Long commentId) {
        return ApiResponse.success(replyService.listReplies(postId, commentId));
    }
}