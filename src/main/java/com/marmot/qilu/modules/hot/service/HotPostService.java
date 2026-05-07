package com.marmot.qilu.modules.hot.service;

import com.marmot.qilu.common.event.comment.CommentEvent;
import com.marmot.qilu.common.event.like.LikeEvent;
import com.marmot.qilu.common.event.reply.ReplyEvent;
import com.marmot.qilu.modules.post.dto.PostPageQueryDTO;
import com.marmot.qilu.modules.post.vo.PostPageItemVO;

import java.util.List;

public interface HotPostService {

    void increaseByLike(LikeEvent event);

    void increaseByComment(CommentEvent event);

    void increaseByReply(ReplyEvent event);

    List<PostPageItemVO> getHotPosts(String range, PostPageQueryDTO dto);
}
