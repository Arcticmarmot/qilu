package com.marmot.qilu.modules.search.service;

import com.marmot.qilu.modules.post.vo.PostPageItemVO;
import com.marmot.qilu.modules.post.vo.PostPageVO;
import com.marmot.qilu.modules.search.dto.PostSearchPageQueryDTO;
import com.marmot.qilu.modules.search.model.PostSearchCandidate;

import java.util.List;

public interface PostSearchService {

    void syncPostIndex(List<Long> postIds);

    void deletePostIndex(List<Long> postIds);

    PostPageVO<PostPageItemVO> searchPosts(PostSearchPageQueryDTO dto);

    List<PostSearchCandidate> searchCandidates(String keyword, int limit);
}
