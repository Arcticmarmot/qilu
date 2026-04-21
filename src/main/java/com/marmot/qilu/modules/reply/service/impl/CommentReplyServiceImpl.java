package com.marmot.qilu.modules.reply.service.impl;

import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.modules.reply.dto.CommentReplyCreateDTO;
import com.marmot.qilu.modules.reply.mapper.CommentReplyMapper;
import com.marmot.qilu.modules.reply.service.CommentReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentReplyServiceImpl implements CommentReplyService {

    private static final int STATUS_NORMAL = 1;

    private final CommentReplyMapper commentReplyMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCommentReply(Long postId, Long rootCommentId, CommentReplyCreateDTO dto) {
        String currUserUuid = UserContext.requireUuid();


    }
}
