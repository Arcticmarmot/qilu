package com.marmot.qilu.modules.search.repository;

import com.marmot.qilu.modules.search.document.PostSearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface PostSearchRepository extends ElasticsearchRepository<PostSearchDocument, Long> {
}
