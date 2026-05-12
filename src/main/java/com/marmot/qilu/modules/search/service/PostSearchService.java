package com.marmot.qilu.modules.search.service;

import com.marmot.qilu.modules.post.vo.PostPageItemVO;
import java.util.List;

public interface PostSearchService {

    void syncPostIndex(List<Long> postIds);

    void deletePostIndex(List<Long> postIds);

    List<PostPageItemVO> searchPosts(String keyword);
}
