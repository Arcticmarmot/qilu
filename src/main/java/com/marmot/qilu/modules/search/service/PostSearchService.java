package com.marmot.qilu.modules.search.service;

import com.marmot.qilu.modules.post.vo.PostPageItemVO;
import java.util.List;

public interface PostSearchService {

    void syncPostIndex(Long post);

    void deletePostIndex(Long postId);

    List<PostPageItemVO> searchPosts(String keyword);
}
