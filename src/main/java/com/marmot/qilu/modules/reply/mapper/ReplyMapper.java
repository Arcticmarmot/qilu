package com.marmot.qilu.modules.reply.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.marmot.qilu.modules.reply.entity.Reply;
import com.marmot.qilu.modules.reply.vo.ReplyListItemVO;
import com.marmot.qilu.modules.reply.vo.ReplyPreview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReplyMapper extends BaseMapper<Reply> {

    Integer existsInteractableReplyById(@Param("postId") Long postId,
                                        @Param("commentId") Long commentId,
                                        @Param("replyId") Long replyId);

    String selectUserUuidById(@Param("replyId") Long replyId);

    ReplyPreview selectReplyPreviewById(@Param("replyId") Long replyId);

    int deleteReply(@Param("replyId") Long replyId,
                    @Param("currUserUuid") String currUserUuid);

    List<ReplyListItemVO> selectNormalRepliesByCommentId(@Param("currUserUuid") String currUserUuid,
                                                         @Param("commentId") Long commentId);

    int increaseReplyLikeCount(@Param("replyId") Long replyId);

    int decreaseReplyLikeCount(@Param("replyId") Long replyId);
}
