package com.marmot.qilu.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI qiluOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("QiLu API")
                        .description("QiLu 是一个内容社区")
                        .version("v1.0.0")
                        .contact(new Contact().name("Marmot Cyber"))
                );
    }
}
