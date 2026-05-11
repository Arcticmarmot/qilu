package com.marmot.qilu.modules.search.service.impl;

import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.modules.post.model.PostSearchSource;
import com.marmot.qilu.modules.post.service.PostService;
import com.marmot.qilu.modules.post.vo.PostPageItemVO;
import com.marmot.qilu.modules.search.document.PostSearchDocument;
import com.marmot.qilu.modules.search.repository.PostSearchRepository;
import com.marmot.qilu.modules.search.service.PostSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
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
    private static final int SEARCH_RESULT_SIZE = 20;

    private final PostService postService;
    private final PostSearchRepository postSearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public void syncPostIndex(Long postId) {
        validatePostId(postId);

        PostSearchSource source = postService.getPostSearchSource(postId);
        if(!isSearchable(source)) {
            log.warn("sync post index failed, post is not searchable, postId={}", postId);
            return;
        }

        PostSearchDocument document = convertToDocument(source);
        postSearchRepository.save(document);

        log.info("index post success, postId={}", postId);
    }

    @Override
    public void deletePostIndex(Long postId) {
        validatePostId(postId);
        postSearchRepository.deleteById(postId);
        log.info("delete post index success, postId={}", postId);
    }

    @Override
    public List<PostPageItemVO> searchPosts(String keyword) {
        validateKeyword(keyword);

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
                .withPageable(PageRequest.of(0, SEARCH_RESULT_SIZE))
                .build();
        SearchHits<PostSearchDocument> searchHits = elasticsearchOperations.search(query, PostSearchDocument.class);
        List<Long> postIds = searchHits.getSearchHits()
                .stream()
                .map(hit -> hit.getContent().getRootId())
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if(postIds.isEmpty()) {
            return List.of();
        }
        return postService.getPublicPostsByIds(postIds);
    }


    private boolean isSearchable(PostSearchSource source) {
        return source != null
                && Integer.valueOf(STATUS_NORMAL).equals(source.getStatus())
                && Integer.valueOf(VISIBILITY_PUBLIC).equals(source.getVisibility());
    }

    private void deletePostIndexAtRepository(Long postId) {

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

    private void validatePostId(Long postId) {
        if(postId == null || postId <= 0) {
            throw new BadRequestException("post id must not be blank");
        }
    }

    private void validateKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            throw new BadRequestException("keyword is blank");
        }
    }
}
