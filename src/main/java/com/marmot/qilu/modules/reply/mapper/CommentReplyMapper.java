package com.marmot.qilu.modules.reply.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.marmot.qilu.modules.reply.entity.CommentReply;
import com.marmot.qilu.modules.reply.vo.CommentReplyListItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentReplyMapper extends BaseMapper<CommentReply> {

    Integer existsInteractableCommentReplyById(@Param("postId") Long postId,
                                           @Param("commentId") Long commentId,
                                           @Param("replyId") Long replyId);

    String selectUserUuidById(@Param("replyId") Long replyId);

    int deleteCommentReply(@Param("replyId") Long replyId,
                          @Param("currUserUuid") String currUserUuid);

    List<CommentReplyListItemVO> selectNormalCommentRepliesByCommentId(@Param("currUserUuid") String currUserUuid, @Param("commentId") Long commentId);

    int increaseReplyLikeCount(@Param("replyId") Long replyId);

    int decreaseReplyLikeCount(@Param("replyId") Long replyId);
}
