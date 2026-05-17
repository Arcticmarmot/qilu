package com.marmot.qilu.modules.admin.like.service;

import com.marmot.qilu.modules.admin.like.dto.AdminLikePageQueryDTO;
import com.marmot.qilu.modules.admin.like.vo.AdminLikePageItemVO;
import com.marmot.qilu.modules.admin.like.vo.AdminLikePageVO;

public interface AdminLikeService {

    AdminLikePageVO<AdminLikePageItemVO> getLikePage(AdminLikePageQueryDTO dto);
}