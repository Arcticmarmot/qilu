package com.marmot.qilu.modules.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.marmot.qilu.modules.post.entity.Post;
import com.marmot.qilu.modules.post.model.PostCreatedAtItem;
import com.marmot.qilu.modules.post.model.PostSearchSource;
import com.marmot.qilu.modules.post.vo.PostDetailVO;
import com.marmot.qilu.modules.post.vo.PostPageItemVO;
import com.marmot.qilu.modules.post.model.PostPreview;
import com.marmot.qilu.modules.post.model.PostTreeInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    Integer existsInteractablePostById(@Param("postId") Long postId, @Param("currUserUuid") String currUserUuid);

    Integer existsNormalBranchPostById(@Param("postId") Long postId);

    String selectUserUuidById(@Param("postId") Long postId);

    PostSearchSource selectPostSearchSourceById(@Param("postId") Long postId);

    PostTreeInfo selectPostTreeInfo(@Param("postId") Long postId);

    List<Long> selectSubtreePostIds(@Param("postId") Long postId);

    int updateRootIdByIds(@Param("postIds") List<Long> postIds, @Param("rootId") Long rootId);

    List<PostPageItemVO> selectPublicPostByIds(@Param("currUserUuid") String currUserUuid, @Param("postIds") List<Long> postIds);

    List<PostCreatedAtItem> selectPublicPostCreatedAtItemByIds(@Param("postIds") List<Long> postIds);

    PostPreview selectPostPreviewById(@Param("postId") Long postId);

    Long countMyPosts(@Param("currUserUuid") String currUserUuid);

    List<PostDetailVO> selectMyPostDetail(@Param("postId") Long postId,
                                        @Param("currUserUuid") String currUserUuid);

    List<PostPageItemVO> selectMyPostPage(@Param("offset") long offset,
                                          @Param("size") long size,
                                          @Param("currUserUuid") String currUserUuid);

    Long countPublicPosts();

    List<PostDetailVO> selectPublicPostDetail(@Param("postId") Long postId,
                                  @Param("currUserUuid") String currUserUuid);

    List<PostPageItemVO> selectPublicPostPage(@Param("offset") long offset,
                                              @Param("size") long size,
                                              @Param("currUserUuid") String currUserUuid);

    int increasePostLikeCount(@Param("postId") Long postId);

    int decreasePostLikeCount(@Param("postId") Long postId);

    int increasePostCommentCount(@Param("postId") Long postId);

    int decreasePostCommentCount(@Param("postId") Long postId);
}
