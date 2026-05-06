package com.marmot.qilu.modules.hot.service;

import com.marmot.qilu.common.event.comment.CommentEvent;
import com.marmot.qilu.common.event.like.LikeEvent;
import com.marmot.qilu.common.event.reply.ReplyEvent;

public interface HotPostService {

    void increaseByLike(LikeEvent event);

    void increaseByComment(CommentEvent event);

    void increaseByReply(ReplyEvent event);


//    List<HotPostItemVO> listHotPosts(String range, Integer current, Integer size);
}
