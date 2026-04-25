package com.marmot.qilu.modules.comment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@TableName("post_comment")
@Getter
@Setter
public class PostComment {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;

    private String postAuthorUuid;

    private String userUuid;

    private String content;

    private Integer likeCount;

    private Integer replyCount;

    /**
     * 0-删除 1-正常
     */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
