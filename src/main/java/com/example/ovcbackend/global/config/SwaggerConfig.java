package com.example.ovcbackend.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        //swagger에는 jwt 인증 설정이 가능함

        String jwtSchemaName = "jwtAuth";
        // api 요청 시 사용할 보안 요구사항 설정
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemaName);

        // components에 jwt 보안 스키마 추가
        Components components = new Components().addSecuritySchemes(jwtSchemaName, new SecurityScheme()
                .name(jwtSchemaName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT"));

        // api 정보 설정
        Info info = new Info()
                .version("v1.0.0")
                .title("ovc 자격증 통합 프로젝트 swagger")
                .description("ovc 자격증 통합 프로젝트 api 명세서 입니다.");

        // 서버 설정 (base URL을 설정)
        io.swagger.v3.oas.models.servers.Server server =
                new io.swagger.v3.oas.models.servers.Server().url("/");

        // api 정보
        OpenAPI openApi = new OpenAPI()
                .addServersItem(server)
                .addSecurityItem(securityRequirement)
                .components(components)
                .info(info);

        return openApi;
    }
}
