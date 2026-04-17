package com.marmot.qilu.modules.like.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.marmot.qilu.modules.like.entity.PostLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PostLikeMapper extends BaseMapper<PostLike> {

    Integer countNormalPostById(@Param("postId") Long postId);

    int increasePostLikeCount(@Param("postId") Long postId);

    int decreasePostLikeCount(@Param("postId") Long postId);

    Integer selectPostLikeCount(@Param("postId") Long postId);
}
