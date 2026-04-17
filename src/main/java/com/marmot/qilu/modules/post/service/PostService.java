package com.marmot.qilu.modules.post.service;

import com.marmot.qilu.modules.post.dto.PostCreateDTO;
import com.marmot.qilu.modules.post.dto.PostPageQueryDTO;
import com.marmot.qilu.modules.post.dto.PostUpdateDTO;
import com.marmot.qilu.modules.post.vo.PostDetailVO;
import com.marmot.qilu.modules.post.vo.PostPageItemVO;
import com.marmot.qilu.modules.post.vo.PostPageVO;

public interface PostService {

    Long createPost(PostCreateDTO dto);

    PostDetailVO getPublicPostDetail(Long postId);

    void updatePost(Long postId, PostUpdateDTO dto);

    void deletePost(Long postId);

    PostPageVO<PostPageItemVO> getPublicPostPage(PostPageQueryDTO dto);
}
