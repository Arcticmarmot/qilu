package com.marmot.qilu.modules.like.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("post_like")
public class PostLike {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;

    private String userUuid;

    /**
     * 0-取消点赞 1-已点赞
     */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
