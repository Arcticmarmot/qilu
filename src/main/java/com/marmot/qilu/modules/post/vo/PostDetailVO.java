package com.marmot.qilu.modules.post.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Schema(description = "帖子详情")
public class PostDetailVO {

    @Schema(description = "帖子ID", example = "102")
    private Long id;

    @Schema(description = "父帖子ID", example = "101")
    private Long parentId;

    @Schema(description = "根帖子ID", example = "100")
    private Long rootId;

    @Schema(description = "分支对话", example = "去哪里")
    private String branchPrompt;

    @Schema(description = "作者用户UUID")
    private String userUuid;

    @Schema(description = "作者昵称", example = "marmot")
    private String nickname;

    @Schema(description = "帖子标题", example = "春天")
    private String title;

    @Schema(description = "帖子图片信息组")
    private List<PostMediaVO> mediaList;

    @Schema(description = "帖子正文")
    private String content;

    @Schema(description = "可见性：1公开 2仅自己", example = "1")
    private Integer visibility;

    @Schema(description = "点赞数", example = "3")
    private Integer likeCount;

    @Schema(description = "当前登录用户是否已点赞", example = "true")
    private Boolean likedByMe;

    @Schema(description = "创建时间")
    private LocalDateTime createAt;
}
