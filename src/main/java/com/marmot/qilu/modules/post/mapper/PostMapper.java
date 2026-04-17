package com.marmot.qilu.modules.post.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.marmot.qilu.modules.post.entity.Post;
import com.marmot.qilu.modules.post.vo.PostPageItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    Long countPublicPosts();

    List<PostPageItemVO> selectPublicPostPage(@Param("offset") long current, @Param("size") long size);
}
