package com.marmot.qilu.modules.admin.comment.service;

import com.marmot.qilu.modules.admin.comment.dto.AdminCommentPageQueryDTO;
import com.marmot.qilu.modules.admin.comment.vo.AdminCommentPageItemVO;
import com.marmot.qilu.modules.admin.comment.vo.AdminCommentPageVO;

public interface AdminCommentService {

    AdminCommentPageVO<AdminCommentPageItemVO> getCommentPage(AdminCommentPageQueryDTO dto);

    void banComment(Long commentId);

    void unbanComment(Long commentId);
}
