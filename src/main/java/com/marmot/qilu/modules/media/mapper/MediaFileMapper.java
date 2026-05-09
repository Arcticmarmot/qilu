package com.marmot.qilu.modules.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.marmot.qilu.modules.media.entity.MediaFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MediaFileMapper extends BaseMapper<MediaFile> {

    int markMediaFilesUsed(@Param("currUserUuid") String currUserUuid, @Param("mediaIds") List<Long> mediaIds);
}
