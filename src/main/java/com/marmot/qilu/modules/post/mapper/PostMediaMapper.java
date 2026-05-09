package com.marmot.qilu.modules.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.marmot.qilu.modules.post.entity.PostMedia;
import com.marmot.qilu.modules.post.vo.PostMediaVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostMediaMapper extends BaseMapper<PostMedia> {

    List<PostMediaVO> selectCoverMediaByPostIds(@Param("postIds") List<Long> postIds);

    List<PostMediaVO> selectPostMediaListById(@Param("postId") Long postId);
}
