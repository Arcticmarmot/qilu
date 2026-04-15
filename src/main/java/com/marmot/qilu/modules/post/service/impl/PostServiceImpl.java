package com.marmot.qilu.modules.post.service.impl;

import com.marmot.qilu.modules.post.dto.PostCreateDTO;
import com.marmot.qilu.modules.post.dto.PostPageQueryDTO;
import com.marmot.qilu.modules.post.service.PostService;
import com.marmot.qilu.modules.post.vo.PostDetailVO;
import com.marmot.qilu.modules.post.vo.PostPageItemVO;
import com.marmot.qilu.modules.post.vo.PostPageVO;
import org.springframework.stereotype.Service;

@Service
public class PostServiceImpl implements PostService {
    @Override
    public Long createPost(PostCreateDTO dto) {
        return 0L;
    }

    @Override
    public PostDetailVO getPostDetail(Long postId) {
        return null;
    }

    @Override
    public PostPageVO<PostPageItemVO> getPostPage(PostPageQueryDTO dto) {
        return null;
    }

    @Override
    public void deletePost(Long postId) {

    }
}
