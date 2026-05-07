package com.marmot.qilu.modules.post.service;

import com.marmot.qilu.modules.post.dto.PostCreateDTO;
import com.marmot.qilu.modules.post.dto.PostPageQueryDTO;
import com.marmot.qilu.modules.post.dto.PostUpdateDTO;
import com.marmot.qilu.modules.post.vo.PostDetailVO;
import com.marmot.qilu.modules.post.vo.PostPageItemVO;
import com.marmot.qilu.modules.post.vo.PostPageVO;
import com.marmot.qilu.modules.post.vo.PostPreview;

import java.util.List;

public interface PostService {

    void checkPostInteractable(Long postId, String currUserUuid);

    String getAuthorUuid(Long postId);

    List<PostPageItemVO> getPostsByIds(List<Long> postIds);

    PostPreview getPostPreview(Long postId);

    void createPost(PostCreateDTO dto);

    PostDetailVO getMyPostDetail(Long postId);

    PostPageVO<PostPageItemVO> getMyPostPage(PostPageQueryDTO dto);

    PostDetailVO getPublicPostDetail(Long postId);

    PostPageVO<PostPageItemVO> getPublicPostPage(PostPageQueryDTO dto);

    void updatePost(Long postId, PostUpdateDTO dto);

    void deletePost(Long postId);

    int increasePostLikeCount(Long postId);

    int decreasePostLikeCount(Long postId);

    int increasePostCommentCount(Long postId);

    int decreasePostCommentCount(Long postId);
}
