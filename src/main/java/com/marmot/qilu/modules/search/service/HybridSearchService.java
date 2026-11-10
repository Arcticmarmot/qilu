package com.marmot.qilu.modules.search.service;

import com.marmot.qilu.modules.search.dto.HybridSearchQueryDTO;
import com.marmot.qilu.modules.search.vo.HybridSearchItemVO;

import java.util.List;

public interface HybridSearchService {

    List<HybridSearchItemVO> searchPosts(HybridSearchQueryDTO dto);
}
