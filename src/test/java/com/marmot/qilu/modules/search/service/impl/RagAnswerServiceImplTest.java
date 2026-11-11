package com.marmot.qilu.modules.search.service.impl;

import com.marmot.qilu.common.api.ErrorCode;
import com.marmot.qilu.common.exception.BizException;
import com.marmot.qilu.common.exception.NotFoundException;
import com.marmot.qilu.modules.post.service.PostService;
import com.marmot.qilu.modules.post.vo.PostDetailVO;
import com.marmot.qilu.modules.post.vo.PostPageItemVO;
import com.marmot.qilu.modules.search.client.OllamaChatClient;
import com.marmot.qilu.modules.search.dto.RagChatRequestDTO;
import com.marmot.qilu.modules.search.service.HybridSearchService;
import com.marmot.qilu.modules.search.vo.HybridSearchItemVO;
import com.marmot.qilu.modules.search.vo.RagAnswerVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RagAnswerServiceImplTest {

    @Mock
    private HybridSearchService hybridSearchService;
    @Mock
    private PostService postService;
    @Mock
    private OllamaChatClient ollamaChatClient;

    private RagAnswerServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RagAnswerServiceImpl(hybridSearchService, postService, ollamaChatClient);
    }

    @Test
    void answersWithFullNodeBodyBranchContextAndDatabaseReferences() {
        PostDetailVO root = node(100L, null, "根帖背景");
        root.setTitle("数据库中的 Kafka 标题");
        PostDetailVO parent = node(101L, 100L, "父节点讨论 Offset");
        String fullBody = "正文".repeat(100) + "这段完整正文位于 160 字之后：重试必须配合幂等。";
        PostDetailVO matched = node(102L, 101L, fullBody);
        matched.setBranchPrompt("重试后重复消费怎么办？");
        when(hybridSearchService.searchPosts(any())).thenReturn(List.of(hit(100L, 102L), hit(100L, 101L)));
        when(postService.getPublicPostDetail(100L)).thenReturn(List.of(root, parent, matched));
        when(ollamaChatClient.chat(anyString(), anyString())).thenReturn("重试时应配合幂等处理。[1]");

        RagAnswerVO result = service.answer(request());

        assertEquals("重试时应配合幂等处理。[1]", result.answer());
        assertEquals(1, result.references().size());
        assertEquals(100L, result.references().get(0).postId());
        assertEquals(102L, result.references().get(0).matchedNodeId());
        assertEquals("数据库中的 Kafka 标题", result.references().get(0).title());
        assertEquals("/posts/100", result.references().get(0).url());
        ArgumentCaptor<String> system = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> prompt = ArgumentCaptor.forClass(String.class);
        verify(ollamaChatClient).chat(system.capture(), prompt.capture());
        assertTrue(system.getValue().contains("不可信资料"));
        assertTrue(prompt.getValue().contains(fullBody));
        assertTrue(prompt.getValue().contains("父节点讨论 Offset"));
        assertTrue(prompt.getValue().contains("根帖背景"));
        assertTrue(prompt.getValue().contains("重试后重复消费怎么办？"));
        assertFalse(prompt.getValue().contains("搜索摘要不能作为正文"));
        verify(postService, times(1)).getPublicPostDetail(100L);
    }

    @Test
    void skipsModelWhenRetrievalHasNoResults() {
        when(hybridSearchService.searchPosts(any())).thenReturn(List.of());

        RagAnswerVO result = service.answer(request());

        assertTrue(result.answer().contains("没有找到足够的公开证据"));
        assertTrue(result.references().isEmpty());
        verifyNoInteractions(postService, ollamaChatClient);
    }

    @Test
    void propagatesModelFailureAsExplicitBusinessError() {
        prepareOneSource();
        BizException failure = new BizException(ErrorCode.SYSTEM_ERROR, "Ollama 请求失败或超时");
        when(ollamaChatClient.chat(anyString(), anyString())).thenThrow(failure);

        assertSame(failure, assertThrows(BizException.class, () -> service.answer(request())));
    }

    @Test
    void skipsRemovedRootAndPrivateMatchedNode() {
        when(hybridSearchService.searchPosts(any())).thenReturn(List.of(hit(99L, 99L), hit(100L, 102L)));
        when(postService.getPublicPostDetail(99L)).thenThrow(new NotFoundException("post not found"));
        PostDetailVO privateNode = node(102L, 100L, "私密正文");
        privateNode.setVisibility(2);
        when(postService.getPublicPostDetail(100L)).thenReturn(List.of(node(100L, null, "公开根帖"), privateNode));

        RagAnswerVO result = service.answer(request());

        assertTrue(result.references().isEmpty());
        verifyNoInteractions(ollamaChatClient);
    }

    @Test
    void rejectsCitationNumbersOutsideActualSources() {
        prepareOneSource();
        when(ollamaChatClient.chat(anyString(), anyString())).thenReturn("这个结论来自社区。[1][999999999999999999999]");

        BizException error = assertThrows(BizException.class, () -> service.answer(request()));

        assertTrue(error.getMessage().contains("无效的帖子引用"));
    }

    @Test
    void boundsLongContextAndDoesNotReturnAnUncitedModelAnswer() {
        when(hybridSearchService.searchPosts(any())).thenReturn(List.of(hit(100L, 100L)));
        PostDetailVO root = node(100L, null, "内容".repeat(10_000));
        root.setTitle("标题".repeat(1000));
        root.setBranchPrompt("提问".repeat(1000));
        when(postService.getPublicPostDetail(100L)).thenReturn(List.of(root));
        when(ollamaChatClient.chat(anyString(), anyString())).thenReturn("模型没有提供来源的回答");

        RagAnswerVO result = service.answer(request());

        ArgumentCaptor<String> prompt = ArgumentCaptor.forClass(String.class);
        verify(ollamaChatClient).chat(anyString(), prompt.capture());
        assertTrue(prompt.getValue().length() < 2600);
        assertTrue(result.references().isEmpty());
        assertTrue(result.answer().contains("没有找到足够的公开证据"));
    }

    private void prepareOneSource() {
        when(hybridSearchService.searchPosts(any())).thenReturn(List.of(hit(100L, 100L)));
        when(postService.getPublicPostDetail(100L)).thenReturn(List.of(node(100L, null, "重试与幂等")));
    }

    private RagChatRequestDTO request() {
        RagChatRequestDTO request = new RagChatRequestDTO();
        request.setQuestion("Kafka 消费者失败后如何恢复？");
        return request;
    }

    private HybridSearchItemVO hit(Long rootId, Long matchedId) {
        PostPageItemVO post = new PostPageItemVO();
        post.setId(rootId);
        post.setTitle("旧的索引标题");
        return new HybridSearchItemVO(post, matchedId, "搜索摘要不能作为正文", List.of("ES", "VECTOR"), 0.03);
    }

    private PostDetailVO node(Long id, Long parentId, String content) {
        PostDetailVO node = new PostDetailVO();
        node.setId(id);
        node.setParentId(parentId);
        node.setRootId(100L);
        node.setVisibility(1);
        node.setTitle("Kafka 重试");
        node.setContent(content);
        return node;
    }
}
