package com.example.ovcbackend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// customoauth2userservice에서 인코더 springConfig랑 분리를 안하니 순환 참조로 서버에 오류가 발생해버렸음
// 그래서 security config에서 패스워드 인코더만 분리
@Configuration
public class PasswordConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
