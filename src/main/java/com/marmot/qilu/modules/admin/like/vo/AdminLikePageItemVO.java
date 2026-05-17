package com.marmot.qilu.modules.admin.like.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "管理端点赞分页项")
public class AdminLikePageItemVO {

    @Schema(description = "点赞ID")
    private Long id;

    @Schema(description = "创作类型：POST、COMMENT、REPLY")
    private String creationType;

    @Schema(description = "创作ID")
    private Long creationId;

    @Schema(description = "用户UUID")
    private String userUuid;

    @Schema(description = "状态：0-取消点赞 1-已点赞")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}