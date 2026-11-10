package com.marmot.qilu.modules.search.service;

import java.util.List;

public interface EmbeddingService {

    List<List<Float>> embed(List<String> texts);
}
