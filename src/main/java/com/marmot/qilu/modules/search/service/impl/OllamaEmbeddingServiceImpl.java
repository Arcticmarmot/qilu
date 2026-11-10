package com.marmot.qilu.modules.search.service.impl;

import com.marmot.qilu.common.config.EmbeddingProperties;
import com.marmot.qilu.modules.search.service.EmbeddingService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(prefix = "milvus", name = "enabled", havingValue = "true")
public class OllamaEmbeddingServiceImpl implements EmbeddingService {

    private final EmbeddingProperties properties;
    private final RestClient restClient;

    public OllamaEmbeddingServiceImpl(EmbeddingProperties properties) {
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

    @Override
    public List<List<Float>> embed(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return List.of();
        }

        EmbeddingResponse response = restClient.post()
                .uri("/api/embed")
                .body(Map.of(
                        "model", properties.getModel(),
                        "input", texts,
                        "dimensions", properties.getDimension(),
                        "truncate", false
                ))
                .retrieve()
                .body(EmbeddingResponse.class);

        if (response == null || response.embeddings() == null
                || response.embeddings().size() != texts.size()) {
            throw new IllegalStateException("embedding response size does not match input");
        }
        for (List<Float> vector : response.embeddings()) {
            if (vector == null || vector.size() != properties.getDimension()
                    || vector.stream().anyMatch(value -> value == null || !Float.isFinite(value))) {
                throw new IllegalStateException("embedding vector is invalid or has the wrong dimension");
            }
        }
        return response.embeddings();
    }

    public record EmbeddingResponse(List<List<Float>> embeddings) {
    }
}
