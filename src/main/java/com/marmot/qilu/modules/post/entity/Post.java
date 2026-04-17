package com.marmot.qilu.modules.post.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@TableName("post")
public class Post {
    @TableId(type= IdType.AUTO)
    private Long id;

    private String userUuid;

    private String title;

    private String content;

    /**
     * 0 deleted
     * 1 normal
     */
    private Integer status;

    /**
     * 1 public
     * 2 private
     */
    private Integer visibility;

    private Integer likeCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;
}
