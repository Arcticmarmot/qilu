package com.marmot.qilu.modules.post.service;

import com.marmot.qilu.modules.post.dto.PostPageQueryDTO;
import com.marmot.qilu.modules.post.model.PostPreview;
import com.marmot.qilu.modules.post.model.PostSearchSource;
import com.marmot.qilu.modules.post.vo.PostDetailVO;
import com.marmot.qilu.modules.post.vo.PostPageItemVO;
import com.marmot.qilu.modules.post.vo.PostPageVO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface PostQueryService {

    void checkPostInteractable(Long postId, String currUserUuid);

    List<PostSearchSource> getPostSearchSourceList(List<Long> postIds);

    List<PostPageItemVO> getPublicPostsByIds(List<Long> postIds);

    Map<Long, LocalDateTime> getPublicPostCreatedAtMapByIds(List<Long> postIds);

    PostPreview getPostPreview(Long postId);

    List<PostDetailVO> getMyPostDetail(Long postId);

    List<PostDetailVO> getPublicPostDetail(Long postId);

    PostPageVO<PostPageItemVO> getMyPostPage(PostPageQueryDTO dto);

    PostPageVO<PostPageItemVO> getPublicPostPage(PostPageQueryDTO dto);
}
