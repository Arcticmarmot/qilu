package com.marmot.qilu.common.config;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

@Slf4j
@Configuration
@EnableConfigurationProperties(MilvusProperties.class)
public class MilvusConfig {

    @Bean
    public ApplicationRunner logMilvusIndexStatus(MilvusProperties properties,
                                                 KafkaListenerEndpointRegistry listenerRegistry) {
        return args -> {
            if (!properties.isEnabled()) {
                log.info("Milvus vector indexing disabled: milvus.enabled=false; "
                        + "set MILVUS_ENABLED=true to enable the vector consumer");
                return;
            }
            var listener = listenerRegistry.getListenerContainer("post-vector-index");
            log.info("Milvus vector indexing enabled, listenerRunning={}, "
                            + "groupId=qilu-post-vector-index-group, uri={}, collection={}",
                    listener != null && listener.isRunning(), properties.getUri(), properties.getCollectionName());
        };
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(prefix = "milvus", name = "enabled", havingValue = "true")
    public MilvusClientV2 milvusClient(MilvusProperties properties) {
        long connectTimeoutMs = properties.getConnectTimeout().toMillis();
        long rpcDeadlineMs = properties.getRpcDeadline().toMillis();
        Assert.isTrue(connectTimeoutMs > 0, "milvus.connect-timeout must be at least 1ms");
        Assert.isTrue(rpcDeadlineMs > 0, "milvus.rpc-deadline must be at least 1ms");

        ConnectConfig.ConnectConfigBuilder builder = ConnectConfig.builder()
                .uri(properties.getUri())
                .dbName(properties.getDatabaseName())
                .connectTimeoutMs(connectTimeoutMs)
                .rpcDeadlineMs(rpcDeadlineMs);

        if (StringUtils.hasText(properties.getToken())) {
            builder.token(properties.getToken());
        }

        return new MilvusClientV2(builder.build());
    }
}
