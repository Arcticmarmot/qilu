package com.marmot.qilu.modules.search.service.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.marmot.qilu.common.config.EmbeddingProperties;
import com.marmot.qilu.common.config.MilvusProperties;
import com.marmot.qilu.common.exception.BadRequestException;
import com.marmot.qilu.modules.post.mapper.PostMapper;
import com.marmot.qilu.modules.post.model.PostSearchSource;
import com.marmot.qilu.modules.post.service.PostService;
import com.marmot.qilu.modules.search.model.PostSearchCandidate;
import com.marmot.qilu.modules.search.service.EmbeddingService;
import com.marmot.qilu.modules.search.service.PostVectorSearchService;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.ConsistencyLevel;
import io.milvus.v2.common.DataType;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import io.milvus.v2.service.vector.request.DeleteReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.UpsertReq;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.SearchResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "milvus", name = "enabled", havingValue = "true")
public class PostVectorSearchServiceImpl implements PostVectorSearchService {

    private static final String PRIMARY_FIELD = "post_id";
    private static final String ROOT_FIELD = "root_id";
    private static final String VECTOR_FIELD = "vector";
    private static final int REBUILD_BATCH_SIZE = 20;

    private final MilvusClientV2 milvusClient;
    private final MilvusProperties milvusProperties;
    private final EmbeddingProperties embeddingProperties;
    private final EmbeddingService embeddingService;
    private final PostService postService;
    private final PostMapper postMapper;

    private volatile boolean collectionReady;

    @Override
    public void syncPostIndex(List<Long> postIds) {
        validatePostIds(postIds);
        if (postIds.isEmpty()) {
            return;
        }
        ensureCollection();

        Map<Long, PostSearchSource> sourceMap = postService.getPostSearchSourceList(postIds).stream()
                .collect(Collectors.toMap(PostSearchSource::getId, source -> source));
        List<PostSearchSource> searchable = new ArrayList<>();
        List<Long> removedIds = new ArrayList<>();
        for (Long postId : postIds) {
            PostSearchSource source = sourceMap.get(postId);
            if (source == null || !Integer.valueOf(1).equals(source.getStatus())
                    || !Integer.valueOf(1).equals(source.getVisibility()) || source.getRootId() == null) {
                removedIds.add(postId);
            } else {
                searchable.add(source);
            }
        }
        deletePostIndex(removedIds);
        if (searchable.isEmpty()) {
            return;
        }

        List<List<Float>> vectors = embeddingService.embed(searchable.stream()
                .map(this::buildEmbeddingText)
                .toList());
        List<JsonObject> rows = new ArrayList<>();
        for (int i = 0; i < searchable.size(); i++) {
            PostSearchSource source = searchable.get(i);
            JsonObject row = new JsonObject();
            row.addProperty(PRIMARY_FIELD, source.getId());
            row.addProperty(ROOT_FIELD, source.getRootId());
            JsonArray vector = new JsonArray();
            vectors.get(i).forEach(vector::add);
            row.add(VECTOR_FIELD, vector);
            rows.add(row);
        }
        milvusClient.upsert(UpsertReq.builder()
                .collectionName(milvusProperties.getCollectionName())
                .data(rows)
                .build());
        log.info("sync post vector index success, indexed={}, removed={}", rows.size(), removedIds.size());
    }

    @Override
    public void deletePostIndex(List<Long> postIds) {
        validatePostIds(postIds);
        if (postIds.isEmpty()) {
            return;
        }
        ensureCollection();
        milvusClient.delete(DeleteReq.builder()
                .collectionName(milvusProperties.getCollectionName())
                .ids(new ArrayList<Object>(postIds))
                .build());
    }

    @Override
    public List<PostSearchCandidate> searchCandidates(String keyword, int limit) {
        if (keyword == null || keyword.isBlank() || limit < 1 || limit > 100) {
            throw new BadRequestException("vector search keyword or limit is invalid");
        }
        ensureCollection();
        List<Float> vector = embeddingService.embed(List.of(keyword.trim())).get(0);
        SearchResp response = milvusClient.search(SearchReq.builder()
                .collectionName(milvusProperties.getCollectionName())
                .annsField(VECTOR_FIELD)
                .data(List.of(new FloatVec(vector)))
                .outputFields(List.of(ROOT_FIELD))
                .limit(limit)
                .build());
        if (response.getSearchResults() == null || response.getSearchResults().isEmpty()) {
            return List.of();
        }
        return response.getSearchResults().get(0).stream()
                .map(hit -> new PostSearchCandidate(
                        Long.valueOf(hit.getId().toString()),
                        hit.getEntity().get(ROOT_FIELD) instanceof Number number
                                ? number.longValue() : null))
                .toList();
    }

    @Override
    public long rebuildPostIndex() {
        ensureCollection();
        long afterId = 0;
        long processed = 0;
        while (true) {
            List<Long> postIds = postMapper.selectPostIdsAfter(afterId, REBUILD_BATCH_SIZE);
            if (postIds.isEmpty()) {
                break;
            }
            syncPostIndex(postIds);
            processed += postIds.size();
            afterId = postIds.get(postIds.size() - 1);
        }
        log.info("rebuild post vector index success, processed={}", processed);
        return processed;
    }

    private synchronized void ensureCollection() {
        if (collectionReady) {
            return;
        }
        String collectionName = milvusProperties.getCollectionName();
        if (!milvusClient.hasCollection(HasCollectionReq.builder().collectionName(collectionName).build())) {
            // Quick setup 自动创建向量索引；root_id 存入动态字段。
            milvusClient.createCollection(CreateCollectionReq.builder()
                    .collectionName(collectionName)
                    .primaryFieldName(PRIMARY_FIELD)
                    .idType(DataType.Int64)
                    .autoID(false)
                    .vectorFieldName(VECTOR_FIELD)
                    .dimension(embeddingProperties.getDimension())
                    .metricType("COSINE")
                    .enableDynamicField(true)
                    .consistencyLevel(ConsistencyLevel.STRONG)
                    .build());
        }
        milvusClient.loadCollection(LoadCollectionReq.builder().collectionName(collectionName).build());
        collectionReady = true;
    }

    private String buildEmbeddingText(PostSearchSource source) {
        return "标题：" + Objects.toString(source.getTitle(), "")
                + "\n分支问题：" + Objects.toString(source.getBranchPrompt(), "")
                + "\n正文：" + Objects.toString(source.getContent(), "");
    }

    private void validatePostIds(List<Long> postIds) {
        if (postIds == null || postIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new BadRequestException("post ids are invalid");
        }
    }
}
