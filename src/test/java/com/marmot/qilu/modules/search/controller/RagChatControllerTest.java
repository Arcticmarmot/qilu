package com.marmot.qilu.modules.search.controller;

import com.marmot.qilu.common.exception.GlobalExceptionHandler;
import com.marmot.qilu.modules.search.service.RagAnswerService;
import com.marmot.qilu.modules.search.vo.RagAnswerVO;
import com.marmot.qilu.modules.search.vo.RagReferenceVO;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class RagChatControllerTest {

    @Test
    void exposesAnswerAndClickableReferencesInExistingResponseEnvelope() throws Exception {
        RagAnswerService service = mock(RagAnswerService.class);
        when(service.answer(any())).thenReturn(new RagAnswerVO("幂等重试。[1]",
                List.of(new RagReferenceVO(100L, 102L, "Kafka 重试", "/posts/100"))));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new RagChatController(service)).build();

        mvc.perform(post("/search/chat").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Kafka 重试\",\"limit\":5}"))
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.answer").value("幂等重试。[1]"))
                .andExpect(jsonPath("$.data.references[0].matchedNodeId").value(102))
                .andExpect(jsonPath("$.data.references[0].url").value("/posts/100"));
    }

    @Test
    void rejectsInvalidQuestionAndLimitBeforeRetrieval() throws Exception {
        RagAnswerService service = mock(RagAnswerService.class);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new RagChatController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();

        mvc.perform(post("/search/chat").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"   \",\"limit\":5}"))
                .andExpect(jsonPath("$.code").value(40000));
        mvc.perform(post("/search/chat").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\":\"Kafka 重试\",\"limit\":6}"))
                .andExpect(jsonPath("$.code").value(40000));
        verifyNoInteractions(service);
    }
}
