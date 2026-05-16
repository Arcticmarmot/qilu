package com.marmot.qilu.modules.hot.service.impl;

import com.marmot.qilu.common.context.UserContext;
import com.marmot.qilu.common.event.comment.CommentEvent;
import com.marmot.qilu.common.event.like.LikeEvent;
import com.marmot.qilu.common.event.reply.ReplyEvent;
import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.modules.hot.service.HotPostService;
import com.marmot.qilu.modules.post.dto.PostPageQueryDTO;
import com.marmot.qilu.modules.post.service.PostService;
import com.marmot.qilu.modules.post.vo.PostPageItemVO;
import com.marmot.qilu.modules.post.vo.PostPageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

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

    private static final String HOT_POST_RAW = "hot::post::raw";
    private static final String HOT_POST_RANK = "hot::post::rank";
    private static final String HOT_POST_RANK_TMP = "hot::post::rank::tmp";

    private static final int REBUILD_CANDIDATE_LIMIT = 1000;
    private static final long DECAY_OFFSET_HOURS = 2;
    private static final double DECAY_FACTOR = 1.2;

    private final StringRedisTemplate stringRedisTemplate;
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
    public PostPageVO<PostPageItemVO> getHotPosts(PostPageQueryDTO dto) {
        if(dto == null) {
            throw new BadRequestException("post page query dto is invalid");
        }

        long current = dto.getCurrent();
        long size = dto.getSize();
        long start = (current - 1) * size;
        long end = start + size - 1;
        long total = Optional.ofNullable(
                stringRedisTemplate.opsForZSet().zCard(HOT_POST_RANK)
        ).orElse(0L);

        Set<String> postIdSet = stringRedisTemplate.opsForZSet().reverseRange(HOT_POST_RANK, start, end);

        if(postIdSet == null || postIdSet.isEmpty()) {
            return new PostPageVO<>(current, size, total, List.of());
        }

        List<Long> postIds = postIdSet.stream().map(Long::valueOf).toList();

        List<PostPageItemVO> records = postService.getPublicPostsByIds(postIds);
        return new PostPageVO<>(current, size, total, records);
    }

    @Override
    public void rebuildHotPostRank() {
        Set<ZSetOperations.TypedTuple<String>> tuples = stringRedisTemplate.opsForZSet()
                .reverseRangeWithScores(HOT_POST_RAW, 0, REBUILD_CANDIDATE_LIMIT - 1);

        if (tuples == null || tuples.isEmpty()) {
            log.debug("rebuild hot post rank skipped, raw ranking is empty");
            return;
        }

        List<Long> postIds = tuples.stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .filter(Objects::nonNull)
                .map(Long::valueOf)
                .toList();

        Map<Long, LocalDateTime> createdAtMap = postService.getPublicPostCreatedAtMapByIds(postIds);

        stringRedisTemplate.delete(HOT_POST_RANK_TMP);

        int rebuilt = 0;

        for(ZSetOperations.TypedTuple<String> tuple: tuples) {
            String postIdValue = tuple.getValue();
            Double rawScore = tuple.getScore();

            if(postIdValue == null || rawScore == null) {
                continue;
            }

            Long postId = Long.valueOf(postIdValue);
            LocalDateTime createdAt = createdAtMap.get(postId);

            if(createdAt == null) {
                continue;
            }

            double rankScore = calculateRankScore(rawScore, createdAt);

            stringRedisTemplate.opsForZSet().add(HOT_POST_RANK_TMP, postIdValue, rankScore);

            rebuilt++;
        }

        if (rebuilt == 0) {
            log.debug("rebuild hot post rank skipped, no valid post found");
            return;
        }

        Boolean renamed = stringRedisTemplate.renameIfAbsent(HOT_POST_RANK_TMP, HOT_POST_RANK);
        if(!renamed) {
            stringRedisTemplate.delete(HOT_POST_RANK);
            stringRedisTemplate.rename(HOT_POST_RANK_TMP, HOT_POST_RANK);
        }

        log.info("rebuild hot post rank success, candidates={}, rebuilt={}", tuples.size(), rebuilt);
    }

    private double calculateRankScore(double rawScore, LocalDateTime createdAt) {
        long ageHours = Duration.between(createdAt, LocalDateTime.now()).toHours();

        if(ageHours < 0) {
            ageHours = 0;
        }

        return rawScore / Math.pow(ageHours + DECAY_OFFSET_HOURS, DECAY_FACTOR);
    }


    private void increasePostScore(Long postId, double score) {
        if (postId == null || postId <= 0) {
            throw new IllegalArgumentException("post id is invalid");
        }

        String member = String.valueOf(postId);

        stringRedisTemplate.opsForZSet().incrementScore(HOT_POST_RAW, member, score);
    }
}
