package com.marmot.qilu.modules.media.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 媒体文件上传结果。
 * 当前 MVP 不落库，所以暂时没有 mediaId。
 * 后续如果增加 media_file 表，可以在这里补充 mediaId 字段。
 */
@Getter
@AllArgsConstructor
public class MediaFileUploadVO {

    private Long mediaId;

    private String objectKey;

    private String url;

    private String originalFilename;

    private String contentType;

    private Long size;
}