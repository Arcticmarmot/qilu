package com.marmot.qilu.modules.admin.like.dto;

import com.marmot.qilu.common.event.like.LikeCreationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "管理端点赞分页查询")
public class AdminLikePageQueryDTO {

    @Schema(description = "页码，从 1 开始", example = "1")
    @Min(value = 1, message = "current page must be >= 1")
    private long current = 1;

    @Schema(description = "每页条数", example = "10")
    @Min(value = 1, message = "page size must be >= 1")
    private long size = 10;

    @Schema(description = "点赞ID", example = "1")
    private Long likeId;

    @Schema(description = "创作类型：POST、COMMENT、REPLY", example = "POST")
    private LikeCreationType creationType;

    @Schema(description = "创作ID", example = "1")
    private Long creationId;

    @Schema(description = "用户UUID")
    private String userUuid;

    @Schema(description = "状态：0-取消点赞 1-已点赞", example = "1")
    private Integer status;
}