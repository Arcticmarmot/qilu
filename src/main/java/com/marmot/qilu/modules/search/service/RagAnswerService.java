package com.marmot.qilu.modules.search.service;

import com.marmot.qilu.modules.search.dto.RagChatRequestDTO;
import com.marmot.qilu.modules.search.vo.RagAnswerVO;

public interface RagAnswerService {

    RagAnswerVO answer(RagChatRequestDTO dto);
}
