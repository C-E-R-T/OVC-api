package com.example.ovcbackend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// OpenAPI sync services use RestTemplate to call external XML APIs.
@Configuration
public class RestTemplateConfig {

    // 외부 OpenAPI 호출에 사용하는 공용 RestTemplate 빈
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
