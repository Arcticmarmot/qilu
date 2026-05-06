package com.marmot.qilu.modules.hot.service.impl;

import com.marmot.qilu.common.event.comment.CommentEvent;
import com.marmot.qilu.common.event.like.LikeEvent;
import com.marmot.qilu.common.event.reply.ReplyEvent;
import com.marmot.qilu.modules.hot.mapper.HotPostMapper;
import com.marmot.qilu.modules.hot.service.HotPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotPostServiceImpl implements HotPostService {

    private static final double LIKE_SCORE = 1.0;
    private static final double COMMENT_SCORE = 3.0;
    private static final double REPLY_SCORE = 2.0;

    public static final String HOT_POST_DAILY = "hot:post:daily";
    public static final String HOT_POST_WEEKLY = "hot:post:weekly";
    public static final String HOT_POST_GLOBAL = "hot:post:global";

    private final StringRedisTemplate stringRedisTemplate;
    private final HotPostMapper hotPostMapper;

    @Override
    public void increaseByLike(LikeEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("like event must not be null");
        }

        switch (event.getCreationType()) {
            case POST -> increasePostScore(event.getCreationId(), LIKE_SCORE);
//            case COMMENT ->
//            case REPLY ->
        }

    }

    @Override
    public void increaseByComment(CommentEvent event) {

    }

    @Override
    public void increaseByReply(ReplyEvent event) {

    }

    private void increasePostScore(Long postId, double score) {
        if (postId == null || postId <= 0) {
            throw new IllegalArgumentException("post id is invalid");
        }

        String member = String.valueOf(postId);

        stringRedisTemplate.opsForZSet().incrementScore(HOT_POST_DAILY, member, score);
        stringRedisTemplate.opsForZSet().incrementScore(HOT_POST_WEEKLY, member, score);
        stringRedisTemplate.opsForZSet().incrementScore(HOT_POST_GLOBAL, member, score);
    }


}
