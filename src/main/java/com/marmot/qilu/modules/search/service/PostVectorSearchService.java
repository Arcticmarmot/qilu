package com.marmot.qilu.modules.search.service;

import com.marmot.qilu.modules.search.model.PostSearchCandidate;

import java.util.List;

public interface PostVectorSearchService {

    void syncPostIndex(List<Long> postIds);

    void deletePostIndex(List<Long> postIds);

    List<PostSearchCandidate> searchCandidates(String keyword, int limit);

    long rebuildPostIndex();
}
