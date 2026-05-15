package com.marmot.qilu.modules.post.service;

import com.marmot.qilu.modules.post.dto.*;
import com.marmot.qilu.modules.post.model.PostSearchSource;
import com.marmot.qilu.modules.post.vo.PostDetailVO;
import com.marmot.qilu.modules.post.vo.PostPageItemVO;
import com.marmot.qilu.modules.post.vo.PostPageVO;
import com.marmot.qilu.modules.post.model.PostPreview;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface PostService {

    Long createPost(PostCreateDTO dto);

    Long createBranchPost(Long parentPostId, BranchPostCreateDTO dto);

    void updatePost(Long postId, PostUpdateDTO dto);

    void updatePostParent(Long postId, PostParentUpdateDTO dto);

    void deletePost(Long postId);

    void checkPostInteractable(Long postId, String currUserUuid);

    List<PostSearchSource> getPostSearchSourceList(List<Long> postIds);

    List<PostPageItemVO> getPublicPostsByIds(List<Long> postIds);

    Map<Long, LocalDateTime> getPublicPostCreatedAtMapByIds(List<Long> postIds);

    PostPreview getPostPreview(Long postId);

    List<PostDetailVO> getMyPostDetail(Long postId);

    PostPageVO<PostPageItemVO> getMyPostPage(PostPageQueryDTO dto);

    List<PostDetailVO> getPublicPostDetail(Long postId);

    PostPageVO<PostPageItemVO> getPublicPostPage(PostPageQueryDTO dto);

    int increasePostLikeCount(Long postId);

    int decreasePostLikeCount(Long postId);

    int increasePostCommentCount(Long postId);

    int decreasePostCommentCount(Long postId);
}
