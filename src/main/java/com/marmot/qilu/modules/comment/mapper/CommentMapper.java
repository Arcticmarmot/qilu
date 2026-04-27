package com.marmot.qilu.modules.comment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.marmot.qilu.modules.comment.entity.Comment;
import com.marmot.qilu.modules.comment.vo.CommentPreview;
import com.marmot.qilu.modules.comment.vo.CommentListItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    Integer existsInteractableCommentById(@Param("postId") Long postId,
                                          @Param("commentId") Long commentId);

    String selectUserUuidById(@Param("commentId") Long commentId);

    CommentPreview selectCommentPreviewById(@Param("commentId") Long commentId);

    int deleteComment(@Param("commentId") Long commentId,
                      @Param("currUserUuid") String currUserUuid);

    List<CommentListItemVO> selectNormalCommentsByPostId(@Param("currUserUuid") String currUserUuid, @Param("postId") Long postId);

    int increaseCommentLikeCount(@Param("commentId") Long commentId);

    int decreaseCommentLikeCount(@Param("commentId") Long commentId);

    int increaseCommentReplyCount(@Param("commentId") Long commentId);

    int decreaseCommentReplyCount(@Param("commentId") Long commentId);
}
