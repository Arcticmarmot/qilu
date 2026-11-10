package com.marmot.qilu.modules.search.service.impl;

import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.modules.post.model.PostSearchSource;
import com.marmot.qilu.modules.post.service.PostService;
import com.marmot.qilu.modules.post.vo.PostPageItemVO;
import com.marmot.qilu.modules.search.dto.HybridSearchQueryDTO;
import com.marmot.qilu.modules.search.model.PostSearchCandidate;
import com.marmot.qilu.modules.search.service.HybridSearchService;
import com.marmot.qilu.modules.search.service.PostSearchService;
import com.marmot.qilu.modules.search.service.PostVectorSearchService;
import com.marmot.qilu.modules.search.vo.HybridSearchItemVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class HybridSearchServiceImpl implements HybridSearchService {

    private static final int CANDIDATE_LIMIT = 50;
    private static final int RRF_CONSTANT = 60;

    private final PostSearchService postSearchService;
    private final ObjectProvider<PostVectorSearchService> vectorServiceProvider;
    private final PostService postService;
    private final Executor executor;

    public HybridSearchServiceImpl(PostSearchService postSearchService,
                                   ObjectProvider<PostVectorSearchService> vectorServiceProvider,
                                   PostService postService,
                                   @Qualifier("hybridSearchExecutor") Executor executor) {
        this.postSearchService = postSearchService;
        this.vectorServiceProvider = vectorServiceProvider;
        this.postService = postService;
        this.executor = executor;
    }

    @Override
    public List<HybridSearchItemVO> searchPosts(HybridSearchQueryDTO dto) {
        if (dto == null || dto.getKeyword() == null || dto.getKeyword().isBlank()
                || dto.getKeyword().length() > 512 || dto.getLimit() < 1 || dto.getLimit() > 50) {
            throw new BadRequestException("hybrid search keyword or limit is invalid");
        }
        String keyword = dto.getKeyword().trim();
        CompletableFuture<List<PostSearchCandidate>> esFuture = recallAsync("ES",
                () -> postSearchService.searchCandidates(keyword, CANDIDATE_LIMIT));
        PostVectorSearchService vectorService = vectorServiceProvider.getIfAvailable();
        CompletableFuture<List<PostSearchCandidate>> vectorFuture = vectorService == null
                ? CompletableFuture.completedFuture(List.of())
                : recallAsync("VECTOR", () -> vectorService.searchCandidates(keyword, CANDIDATE_LIMIT));

        List<PostSearchCandidate> esCandidates = esFuture.join();
        List<PostSearchCandidate> vectorCandidates = vectorFuture.join();
        List<Long> nodeIds = Stream.concat(esCandidates.stream(), vectorCandidates.stream())
                .map(PostSearchCandidate::postId).distinct().toList();
        if (nodeIds.isEmpty()) {
            return List.of();
        }

        // 回表在请求线程执行，保留 UserContext；不信任索引里的旧权限和 rootId。
        Map<Long, PostSearchSource> nodeMap = postService.getPostSearchSourceList(nodeIds).stream()
                .filter(source -> Integer.valueOf(1).equals(source.getStatus())
                        && Integer.valueOf(1).equals(source.getVisibility()) && source.getRootId() != null)
                .collect(Collectors.toMap(PostSearchSource::getId, source -> source));
        List<Long> rootIds = nodeMap.values().stream().map(PostSearchSource::getRootId).distinct().toList();
        if (rootIds.isEmpty()) {
            return List.of();
        }
        Map<Long, PostPageItemVO> postMap = postService.getPublicPostsByIds(rootIds).stream()
                .collect(Collectors.toMap(PostPageItemVO::getId, post -> post));

        Map<Long, FusedCandidate> fused = new HashMap<>();
        addRanks(esCandidates, "ES", nodeMap, postMap, fused);
        addRanks(vectorCandidates, "VECTOR", nodeMap, postMap, fused);
        return fused.values().stream()
                .sorted(Comparator.comparingDouble((FusedCandidate item) -> item.score).reversed()
                        .thenComparing(item -> item.rootId))
                .limit(dto.getLimit())
                .map(item -> new HybridSearchItemVO(postMap.get(item.rootId), item.matchedNode.getId(),
                        buildSnippet(item.matchedNode.getContent()), new ArrayList<>(item.sources), item.score))
                .toList();
    }

    private CompletableFuture<List<PostSearchCandidate>> recallAsync(
            String source, Supplier<List<PostSearchCandidate>> action) {
        try {
            return CompletableFuture.supplyAsync(action, executor)
                    .orTimeout(45, TimeUnit.SECONDS)
                    .exceptionally(error -> {
                        log.warn("hybrid search recall failed, source={}", source, error);
                        return List.of();
                    });
        } catch (RuntimeException error) {
            log.warn("hybrid search recall could not be scheduled, source={}", source, error);
            return CompletableFuture.completedFuture(List.of());
        }
    }

    private void addRanks(List<PostSearchCandidate> candidates, String source,
                          Map<Long, PostSearchSource> nodeMap, Map<Long, PostPageItemVO> postMap,
                          Map<Long, FusedCandidate> fused) {
        Set<Long> seenRoots = new HashSet<>();
        int rank = 0;
        for (PostSearchCandidate candidate : candidates) {
            PostSearchSource node = nodeMap.get(candidate.postId());
            if (node == null || !postMap.containsKey(node.getRootId()) || !seenRoots.add(node.getRootId())) {
                continue;
            }
            rank++;
            FusedCandidate item = fused.computeIfAbsent(node.getRootId(), FusedCandidate::new);
            item.score += 1.0 / (RRF_CONSTANT + rank);
            item.sources.add(source);
            if (rank < item.bestRank) {
                item.bestRank = rank;
                item.matchedNode = node;
            }
        }
    }

    private String buildSnippet(String content) {
        if (content == null) {
            return "";
        }
        String text = content.replaceAll("\\s+", " ").trim();
        return text.substring(0, Math.min(text.length(), 160));
    }

    private static class FusedCandidate {
        private final Long rootId;
        private final Set<String> sources = new LinkedHashSet<>();
        private PostSearchSource matchedNode;
        private int bestRank = Integer.MAX_VALUE;
        private double score;

        private FusedCandidate(Long rootId) {
            this.rootId = rootId;
        }
    }
}
