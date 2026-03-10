package com.example.ovcbackend.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import jakarta.annotation.PostConstruct;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .version("v1.0.0")
                .title("swagger 확인")
                .description("swagger 확인용 api 명세서 입니다.");

        return new OpenAPI().addServersItem(new io.swagger.v3.oas.models.servers.Server().url("/"))
                .components(new Components()).info(info);
    }
}
