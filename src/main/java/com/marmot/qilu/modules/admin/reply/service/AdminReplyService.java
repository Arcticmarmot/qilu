package com.marmot.qilu.modules.admin.reply.service;

import com.marmot.qilu.modules.admin.reply.dto.AdminReplyPageQueryDTO;
import com.marmot.qilu.modules.admin.reply.vo.AdminReplyPageItemVO;
import com.marmot.qilu.modules.admin.reply.vo.AdminReplyPageVO;

public interface AdminReplyService {

    AdminReplyPageVO<AdminReplyPageItemVO> getReplyPage(AdminReplyPageQueryDTO dto);

    void banReply(Long replyId);

    void unbanReply(Long replyId);
}
