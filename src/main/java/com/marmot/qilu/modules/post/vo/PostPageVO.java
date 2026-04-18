package com.marmot.qilu.modules.post.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(description = "分页结果")
public class PostPageVO<T> {

    @Schema(description = "当前页码", example = "1")
    private long current;

    @Schema(description = "每页条数", example = "10")
    private long size;

    @Schema(description = "总记录数", example = "25")
    private long total;

    @Schema(description = "当前页数据")
    private List<T> records;
}
