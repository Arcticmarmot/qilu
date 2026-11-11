package com.marmot.qilu.modules.search.controller;

import com.marmot.qilu.common.api.ApiResponse;
import com.marmot.qilu.modules.search.dto.RagChatRequestDTO;
import com.marmot.qilu.modules.search.service.RagAnswerService;
import com.marmot.qilu.modules.search.vo.RagAnswerVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "RagChat", description = "基于社区公开帖子证据的 AI 问答")
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class RagChatController {

    private final RagAnswerService ragAnswerService;

    @PostMapping("/chat")
    @Operation(summary = "AI 回答", description = "混合检索公开帖子，读取完整正文并调用本地 Ollama，返回回答与真实来源")
    public ApiResponse<RagAnswerVO> chat(@Valid @RequestBody RagChatRequestDTO dto) {
        return ApiResponse.success(ragAnswerService.answer(dto));
    }
}
