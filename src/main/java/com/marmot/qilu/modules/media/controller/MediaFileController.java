package com.marmot.qilu.modules.media.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.modules.media.service.MediaFileService;
import com.marmot.qilu.modules.media.vo.MediaFileUploadVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Media", description = "媒体存储相关接口")
@RestController
@RequestMapping("/media")
@RequiredArgsConstructor
public class MediaFileController {

    private final MediaFileService mediaFileService;

    @Operation(
            summary = "上传帖子图片",
            description = "将当前登录用户上传的图片保存到对象存储中"
    )
    @PostMapping("/images")
    public ApiResponse<MediaFileUploadVO> uploadPostImage(@RequestPart("file")MultipartFile file) {
        MediaFileUploadVO vo = mediaFileService.uploadPostImage(file);
        return ApiResponse.success(vo);
    }
}
