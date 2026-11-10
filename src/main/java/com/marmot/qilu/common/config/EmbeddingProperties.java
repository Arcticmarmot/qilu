package com.marmot.qilu.common.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "embedding")
public class EmbeddingProperties {

    @NotBlank
    private String baseUrl = "http://localhost:11434";

    @NotBlank
    private String model = "qwen3-embedding:0.6b";

    @Min(1)
    private int dimension = 1024;

    @NotNull
    private Duration connectTimeout = Duration.ofSeconds(3);

    @NotNull
    private Duration readTimeout = Duration.ofSeconds(30);
}
