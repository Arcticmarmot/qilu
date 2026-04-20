package com.marmot.qilu.modules.comment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.marmot.qilu.modules.comment.entity.PostComment;
import com.marmot.qilu.modules.comment.vo.PostCommentListItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostCommentMapper extends BaseMapper<PostComment> {

    int deletePostComment(@Param("commentId") Long commentId,
                          @Param("currUserUuid") String currUserUuid);

    List<PostCommentListItemVO> selectNormalCommentsByPostId(@Param("postId") Long postId);
}
