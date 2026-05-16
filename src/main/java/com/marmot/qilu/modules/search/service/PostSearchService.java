package com.marmot.qilu.modules.search.service;

import com.marmot.qilu.modules.post.dto.PostPageQueryDTO;
import com.marmot.qilu.modules.post.vo.PostPageItemVO;
import com.marmot.qilu.modules.post.vo.PostPageVO;
import com.marmot.qilu.modules.search.dto.PostSearchPageQueryDTO;

import java.util.List;

public interface PostSearchService {

    void syncPostIndex(List<Long> postIds);

    void deletePostIndex(List<Long> postIds);

    PostPageVO<PostPageItemVO> searchPosts(PostSearchPageQueryDTO dto);
}
