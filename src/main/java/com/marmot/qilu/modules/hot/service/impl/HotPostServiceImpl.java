package com.marmot.qilu.modules.hot.service.impl;

import com.marmot.qilu.common.event.comment.CommentEvent;
import com.marmot.qilu.common.event.like.LikeEvent;
import com.marmot.qilu.common.event.reply.ReplyEvent;
import com.marmot.qilu.modules.hot.mapper.HotPostMapper;
import com.marmot.qilu.modules.hot.service.HotPostService;
import com.marmot.qilu.modules.post.dto.PostPageQueryDTO;
import com.marmot.qilu.modules.post.service.PostService;
import com.marmot.qilu.modules.post.vo.PostPageItemVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotPostServiceImpl implements HotPostService {

    private static final double LIKE_POST_SCORE = 5.0;
    private static final double LIKE_COMMENT_SCORE = 3.0;
    private static final double LIKE_REPLY_SCORE = 2.0;
    private static final double COMMENT_POST_SCORE = 10.0;
    private static final double REPLY_COMMENT_SCORE = 6.0;
    private static final double REPLY_REPLY_SCORE = 4.0;

    public static final String HOT_POST_DAILY = "hot:post:daily";
    public static final String HOT_POST_WEEKLY = "hot:post:weekly";
    public static final String HOT_POST_GLOBAL = "hot:post:global";

    private final StringRedisTemplate stringRedisTemplate;
    private final HotPostMapper hotPostMapper;
    private final PostService postService;

    @Override
    public void increaseByLike(LikeEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("like event must not be null");
        }

        double score = switch (event.getCreationType()) {
            case POST -> LIKE_POST_SCORE;
            case COMMENT -> LIKE_COMMENT_SCORE;
            case REPLY -> LIKE_REPLY_SCORE;
        };

        increasePostScore(event.getPostId(), score);
    }

    @Override
    public void increaseByComment(CommentEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("comment event must not be null");
        }

        increasePostScore(event.getPostId(), COMMENT_POST_SCORE);
    }

    @Override
    public void increaseByReply(ReplyEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("reply event must not be null");
        }

        double score = switch (event.getCreationType()) {
            case COMMENT -> REPLY_COMMENT_SCORE;
            case REPLY -> REPLY_REPLY_SCORE;
        };

        increasePostScore(event.getPostId(), score);
    }

    @Override
    public List<PostPageItemVO> getHotPosts(String range, PostPageQueryDTO dto) {
        String redisKey = switch (range) {
            case "DAILY" -> HOT_POST_DAILY;
            case "WEEKLY" -> HOT_POST_WEEKLY;
            case "GLOBAL" -> HOT_POST_GLOBAL;
            default -> throw new IllegalStateException("range is invalid");
        };
        long current = dto.getCurrent();
        long size = dto.getSize();
        long start = (current - 1) * size;
        long end = start + size - 1;

        Set<String> postIdSet = stringRedisTemplate.opsForZSet().reverseRange(redisKey, start, end);

        if(postIdSet == null || postIdSet.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = postIdSet.stream().map(Long::valueOf).toList();

        return postService.getPostsByIds(postIds);
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
