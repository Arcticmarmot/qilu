package com.marmot.qilu.modules.search.client;

import com.marmot.qilu.common.api.ErrorCode;
import com.marmot.qilu.common.config.OllamaChatProperties;
import com.marmot.qilu.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class OllamaChatClient {

    private final OllamaChatProperties properties;
    private final RestClient restClient;

    public OllamaChatClient(OllamaChatProperties properties) {
        this.properties = properties;
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.getReadTimeout());
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    public String chat(String systemPrompt, String userPrompt) {
        long started = System.nanoTime();
        try {
            ChatResponse response = restClient.post()
                    .uri("/api/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "model", properties.getModel(),
                            "messages", List.of(new Message("system", systemPrompt), new Message("user", userPrompt)),
                            "stream", false,
                            "think", false,
                            "options", Map.of("temperature", 0.2, "num_predict", 768, "num_ctx", 16384)
                    ))
                    .retrieve()
                    .body(ChatResponse.class);
            if (response == null || response.message() == null
                    || response.message().content() == null || response.message().content().isBlank()
                    || !Boolean.TRUE.equals(response.done())) {
                throw new BizException(ErrorCode.SYSTEM_ERROR, "Ollama 未返回完整回答，请重试");
            }
            log.info("ollama chat succeeded, model={}, elapsedMs={}", properties.getModel(),
                    (System.nanoTime() - started) / 1_000_000);
            return response.message().content().trim();
        } catch (RestClientResponseException e) {
            log.warn("ollama chat HTTP failure, model={}, status={}", properties.getModel(), e.getStatusCode());
            throw new BizException(ErrorCode.SYSTEM_ERROR,
                    "Ollama 调用失败（HTTP " + e.getStatusCode().value() + "），请检查模型和服务配置");
        } catch (RestClientException e) {
            log.warn("ollama chat connection or response failure, model={}", properties.getModel(), e);
            throw new BizException(ErrorCode.SYSTEM_ERROR, "Ollama 请求失败或超时，请检查服务连接后重试");
        }
    }

    public record Message(String role, String content) {
    }

    public record ChatResponse(Message message, Boolean done) {
    }
}
