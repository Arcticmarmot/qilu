package com.marmot.qilu.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.marmot.qilu.modules.search.repository")
public class ElasticsearchConfig {
}
