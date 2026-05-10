package com.marmot.qilu.modules.post.service;

import com.marmot.qilu.modules.post.dto.PostBranchCreateDTO;
import com.marmot.qilu.modules.post.dto.PostCreateDTO;
import com.marmot.qilu.modules.post.dto.PostPageQueryDTO;
import com.marmot.qilu.modules.post.dto.PostUpdateDTO;
import com.marmot.qilu.modules.post.vo.PostDetailVO;
import com.marmot.qilu.modules.post.vo.PostPageItemVO;
import com.marmot.qilu.modules.post.vo.PostPageVO;
import com.marmot.qilu.modules.post.model.PostPreview;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface PostService {

    void checkPostInteractable(Long postId, String currUserUuid);

    String getAuthorUuid(Long postId);

    List<PostPageItemVO> getPublicPostsByIds(List<Long> postIds);

    Map<Long, LocalDateTime> getPublicPostCreatedAtMapByIds(List<Long> postIds);

    PostPreview getPostPreview(Long postId);

    void createPost(PostCreateDTO dto);

    void createBranchPost(Long parentPostId, PostBranchCreateDTO dto);

    List<PostDetailVO> getMyPostDetail(Long postId);

    PostPageVO<PostPageItemVO> getMyPostPage(PostPageQueryDTO dto);

    List<PostDetailVO> getPublicPostDetail(Long postId);

    PostPageVO<PostPageItemVO> getPublicPostPage(PostPageQueryDTO dto);

    void updatePost(Long postId, PostUpdateDTO dto);

    void deletePost(Long postId);

    int increasePostLikeCount(Long postId);

    int decreasePostLikeCount(Long postId);

    int increasePostCommentCount(Long postId);

    int decreasePostCommentCount(Long postId);
}
