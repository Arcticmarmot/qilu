package com.marmot.qilu.modules.like.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.marmot.qilu.modules.like.entity.PostLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PostLikeMapper extends BaseMapper<PostLike> {
}
