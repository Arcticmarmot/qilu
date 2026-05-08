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
public class MediaUploadVO {

    /**
     * 对象存储中的 key。
     * 示例：
     * post-image/2026/05/08/userUuid/uuid.png
     */
    private String objectKey;

    /**
     * 前端可直接访问的公开 URL。
     * MVP 阶段 bucket 公开读，所以直接返回 public url。
     */
    private String url;

    /**
     * 原始文件名。
     * 只用于返回展示，不参与 objectKey 生成，避免路径注入和重名覆盖问题。
     */
    private String originalFilename;

    /**
     * 文件 MIME 类型。
     * 示例：
     * image/png
     */
    private String contentType;

    /**
     * 文件大小，单位 byte。
     */
    private Long size;
}