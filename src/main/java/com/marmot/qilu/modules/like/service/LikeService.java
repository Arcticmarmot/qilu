package com.marmot.qilu.modules.like.service;

import com.marmot.qilu.modules.like.dto.LikeOperateDTO;

public interface LikeService {

    void like(LikeOperateDTO dto);

    void unlike(LikeOperateDTO dto);
}
