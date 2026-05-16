package com.marmot.qilu.modules.search.service.impl;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch.core.search.FieldCollapse;
import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.modules.post.model.PostSearchSource;
import com.marmot.qilu.modules.post.service.PostService;
import com.marmot.qilu.modules.post.vo.PostPageItemVO;
import com.marmot.qilu.modules.post.vo.PostPageVO;
import com.marmot.qilu.modules.search.document.PostSearchDocument;
import com.marmot.qilu.modules.search.dto.PostSearchPageQueryDTO;
import com.marmot.qilu.modules.search.repository.PostSearchRepository;
import com.marmot.qilu.modules.search.service.PostSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostSearchServiceImpl implements PostSearchService {

    private static final int STATUS_NORMAL = 1;
    private static final int VISIBILITY_PUBLIC = 1;
    private static final String AGG_ROOT_COUNT = "root_count";

    private final PostService postService;
    private final PostSearchRepository postSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void syncPostIndex(List<Long> postIds) {
        validatePostIds(postIds);

        List<PostSearchSource> sourceList = postService.getPostSearchSourceList(postIds);
        for(PostSearchSource source: sourceList) {
            Long postId = source.getId();
            if(!isSearchable(source)) {
                postSearchRepository.deleteById(postId);
                log.warn("sync post index failed, post is not searchable, postId={}", postId);
                return;
            }

            PostSearchDocument document = convertToDocument(source);
            postSearchRepository.save(document);
        }


        log.info("index post success, postIds={}", postIds);
    }

    @Override
    public void deletePostIndex(List<Long> postIds) {
        validatePostIds(postIds);
        for(Long postId: postIds) {
            postSearchRepository.deleteById(postId);
        }
        log.info("delete post index success, postIds={}", postIds);
    }

    @Override
    public PostPageVO<PostPageItemVO> searchPosts(PostSearchPageQueryDTO dto) {
        String keyword = dto.getKeyword();
        validateKeyword(keyword);
        int current = dto.getCurrent();
        int size = dto.getSize();

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .bool(b -> b
                                .must(m -> m
                                        .multiMatch(mm -> mm
                                                .query(keyword.trim())
                                                .fields("title^3", "branchPrompt^2", "content")
                                        )
                                )
                                .filter(f -> f
                                        .term(t -> t
                                                .field("status")
                                                .value(STATUS_NORMAL)
                                        )
                                )
                                .filter(f -> f
                                        .term(t -> t
                                                .field("visibility")
                                                .value(VISIBILITY_PUBLIC))
                                )
                        )
                )
                .withFieldCollapse(FieldCollapse.of(fc -> fc
                        .field("rootId")
                ))
                .withPageable(PageRequest.of(current - 1, size))
                .withAggregation(AGG_ROOT_COUNT, Aggregation.of(a -> a
                        .cardinality(c -> c
                            .field("rootId")
                            .precisionThreshold(10000)
                        )
                    )
                )
                .build();

        SearchHits<PostSearchDocument> searchHits = elasticsearchOperations.search(query, PostSearchDocument.class);

        long total = getRootCount(searchHits);

        List<Long> rootIds = searchHits.getSearchHits()
                .stream()
                .map(hit -> hit.getContent().getRootId())
                .filter(Objects::nonNull)
                .toList();

        if(rootIds.isEmpty()) {
            return new PostPageVO<>(current, size, total, List.of());
        }
        List<PostPageItemVO> records = postService.getPublicPostsByIds(rootIds);
        return new PostPageVO<>(current, size, total, records);
    }

    private long getRootCount(SearchHits<PostSearchDocument> searchHits) {
        if (searchHits.getAggregations() == null) {
            return 0L;
        }

        ElasticsearchAggregations aggregations =
                (ElasticsearchAggregations) searchHits.getAggregations();

        ElasticsearchAggregation rootCountAgg = aggregations.get(AGG_ROOT_COUNT);
        if (rootCountAgg == null) {
            return 0L;
        }

        Aggregate aggregate = rootCountAgg.aggregation().getAggregate();
        if (!aggregate.isCardinality()) {
            return 0L;
        }

        double rootCount = aggregate.cardinality().value();
        return Math.round(rootCount);
    }


    private boolean isSearchable(PostSearchSource source) {
        return source != null
                && Integer.valueOf(STATUS_NORMAL).equals(source.getStatus())
                && Integer.valueOf(VISIBILITY_PUBLIC).equals(source.getVisibility());
    }

    private PostSearchDocument convertToDocument(PostSearchSource source) {
        PostSearchDocument document = new PostSearchDocument();

        document.setId(source.getId());
        document.setRootId(source.getRootId());
        document.setTitle(source.getTitle());
        document.setContent(source.getContent());
        document.setBranchPrompt(source.getBranchPrompt());
        document.setUserUuid(source.getUserUuid());
        document.setStatus(source.getStatus());
        document.setVisibility(source.getVisibility());
        document.setCreatedAt(source.getCreatedAt());

        return document;
    }

    private void validatePostIds(List<Long> postIds) {
        for(Long postId: postIds) {
            if(postId == null || postId <= 0) {
                throw new BadRequestException("post id must not be blank");
            }
        }
    }

    private void validateKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new BadRequestException("keyword is blank");
        }
    }
}
