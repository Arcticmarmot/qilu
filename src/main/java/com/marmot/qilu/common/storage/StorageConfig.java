package com.marmot.qilu.common.storage;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {

    private final StorageProperties storageProperties;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(storageProperties.getEndpoint())
                .credentials(
                        storageProperties.getAccessKey(),
                        storageProperties.getSecretKey()
                )
                .build();
    }
}
