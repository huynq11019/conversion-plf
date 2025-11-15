package com.example.docformatting.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI docFormattingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Doc Formatting Platform API")
                        .description("REST API cho dịch vụ chuyển đổi tài liệu")
                        .version("v1"))
                .externalDocs(new ExternalDocumentation()
                        .description("Tài liệu kiến trúc")
                        .url("./docs"));
    }
}
