package com.marmot.qilu.modules.like.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.modules.like.dto.LikeOperateDTO;
import com.marmot.qilu.modules.like.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Like", description = "点赞相关接口")
@RestController
@RequestMapping("/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService postLikeService;

    @Operation(summary = "点赞", description = "当前登录用户对指定实体点赞。重复点赞按幂等处理，不会重复增加点赞数。")
    @PostMapping()
    public ApiResponse<Void> likePost(@Parameter(description = "点赞关联实体信息") LikeOperateDTO dto) {
        postLikeService.like(dto);
        return ApiResponse.success();
    }

    @Operation(summary = "取消点赞", description = "当前登录用户取消对指定实体的点赞。重复取消按幂等处理。")
    @DeleteMapping()
    public ApiResponse<Void> unlike(@Parameter(description = "取消点赞关联实体信息") LikeOperateDTO dto) {
        postLikeService.unlike(dto);
        return ApiResponse.success();
    }
}
