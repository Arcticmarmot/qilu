package com.marmot.qilu.modules.reply.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@TableName("creation_reply")
public class Reply {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;

    private Long rootCommentId;

    private Long parentReplyId;

    private String userUuid;

    private String targetUserUuid;

    private String content;

    private Integer likeCount;

    /**
     * 0-删除 1-正常
     */
    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
