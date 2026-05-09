package com.marmot.qilu.modules.post.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("post_media")
public class PostMedia {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;

    private Long mediaId;

    private Integer sortOrder;

    private LocalDateTime createdAt;
}
