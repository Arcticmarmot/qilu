package com.marmot.qilu.modules.reply.controller;

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
    @PostMapping("/{rootCommentId}/replies")
    public void createCommentReply(@PathVariable Long postId,
                                   @PathVariable Long rootCommentId,
                                   @RequestBody CommentReplyCreateDTO dto) {
        commentReplyService.createCommentReply(postId, rootCommentId, dto);
    }

    @Operation(summary = "获取评论回复列表", description = "获取指定评论的回复列表")
    @GetMapping("/{rootCommentId}/replies")
    public List<CommentReplyListItemVO> listCommentReplies(@PathVariable Long postId,
                                                           @PathVariable Long rootCommentId) {
        return commentReplyService.listCommentReplies(postId, rootCommentId);
    }
}