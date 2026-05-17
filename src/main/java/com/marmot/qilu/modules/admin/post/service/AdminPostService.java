package com.marmot.qilu.modules.admin.post.service;

import com.marmot.qilu.modules.admin.post.dto.AdminPostPageQueryDTO;
import com.marmot.qilu.modules.admin.post.vo.AdminPostPageItemVO;
import com.marmot.qilu.modules.admin.post.vo.AdminPostPageVO;

public interface AdminPostService {

    AdminPostPageVO<AdminPostPageItemVO> getPostPage(AdminPostPageQueryDTO dto);

    void banPost(Long postId);

    void unbanPost(Long postId);
}
