package com.marmot.qilu.modules.comment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.marmot.qilu.modules.comment.entity.PostComment;
import com.marmot.qilu.modules.comment.vo.PostCommentPreview;
import com.marmot.qilu.modules.comment.vo.PostCommentListItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostCommentMapper extends BaseMapper<PostComment> {

    Integer existsInteractablePostCommentById(@Param("postId") Long postId,
                                              @Param("commentId") Long commentId);

    String selectUserUuidById(@Param("commentId") Long commentId);

    PostCommentPreview selectPostCommentPreviewById(@Param("commentId") Long commentId);

    int deletePostComment(@Param("commentId") Long commentId,
                          @Param("currUserUuid") String currUserUuid);

    List<PostCommentListItemVO> selectNormalPostCommentsByPostId(@Param("currUserUuid") String currUserUuid, @Param("postId") Long postId);

    int increaseCommentLikeCount(@Param("commentId") Long commentId);

    int decreaseCommentLikeCount(@Param("commentId") Long commentId);

    int increaseCommentReplyCount(@Param("commentId") Long commentId);

    int decreaseCommentReplyCount(@Param("commentId") Long commentId);
}
