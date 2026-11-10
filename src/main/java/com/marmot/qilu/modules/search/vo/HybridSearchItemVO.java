package com.marmot.qilu.modules.search.vo;

import com.marmot.qilu.modules.post.vo.PostPageItemVO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "混合搜索结果：根帖卡片及实际命中的节点")
public record HybridSearchItemVO(
        PostPageItemVO post,
        Long matchedPostId,
        String matchedSnippet,
        List<String> sources,
        double score
) {
}
