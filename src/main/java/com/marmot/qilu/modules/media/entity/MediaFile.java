package com.marmot.qilu.modules.media.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("media_file")
public class MediaFile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String userUuid;

    private String objectKey;

    private String url;

    private String contentType;

    private Long size;

    /**
     * 0-已删除 1-已使用 2-未使用
     */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
