package com.marmot.qilu.common.config;

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
@ConfigurationProperties(prefix = "milvus")
public class MilvusProperties {

    private boolean enabled = false;

    @NotBlank
    private String uri = "http://localhost:19530";

    private String token;

    @NotBlank
    private String databaseName = "default";

    @NotNull
    private Duration connectTimeout = Duration.ofSeconds(5);

    @NotNull
    private Duration rpcDeadline = Duration.ofSeconds(10);

    @NotBlank
    private String collectionName = "qilu_post_vector";
}
