package com.marmot.qilu.common.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    private String endpoint;

    private String publicEndpoint;

    private String accessKey;

    private String secretKey;

    private String bucket;

    private Long maxFileSize;

    private String postImagePrefix;

    private String avatarPrefix;
}
