package com.example.ovcbackend.global.config;

import com.example.ovcbackend.oauth.CustomOAuth2UserService;
import com.example.ovcbackend.oauth.HttpCookieOAuth2AuthorizationRequestRepository;
import com.example.ovcbackend.global.security.jwt.JwtAuthenticationEntryPoint;
import com.example.ovcbackend.global.security.jwt.JwtAuthenticationFilter;
import com.example.ovcbackend.global.security.jwt.JwtTokenProvider;
import com.example.ovcbackend.oauth.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // Spring Security 인증 매니저 노출
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return  config.getAuthenticationManager();
    }

    // 인증/인가 규칙과 세션 정책 정의
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable()) // JWT는 세션을 안 써서 꺼줌
                // stateless로 두면 네이버 로그인 시도 에러가 나기도 함 방법을 찾아봐야될 듯 사용자가 네이버에서 로그인을 마치고 돌아왔을 때 사용자가 맞나 확인이 필요해짐
                // authorizationRequestRepository에 등록한 쿠키 기반 저장을 통해 해걸!
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션을 사용하지 않겠다고 선언
                // 만약 인증 되지 않은 사용자가 요청을 보내면 JwtAuthenticationEntryPoint가 응답(401)를 보냄.
                // 근데 아직 GlobalException을 설정안했는데 globalException이 exception을 채가지는 않겠지.?
                .exceptionHandling(exception -> exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/login/oauth2/**", "/oauth2/**","/api/admin/sync/**").permitAll()
                        .requestMatchers("/api/certs/**", "/api/calendar/**", "/api/categories").permitAll()
                        .requestMatchers("/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-resources/**",
                                "/webjars/**").permitAll()
                        .requestMatchers("/error").permitAll() // 이거 해줘야 permitAll에서 터진 에러를 보여줌
                        .anyRequest().authenticated() // permitAll 외는 인증 필요
                )
                .oauth2Login(oauth2 -> oauth2.authorizationEndpoint(endpoint -> endpoint
                                .baseUri("/oauth2/authorization") // 로그인 시작 주소
                                .authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository)) // 쿠키 기반 저장소를 사용할 거라고 선언
                        .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService)) // custionOAuth2UserService를 통해 유저 정보를 처리할 거임
                        .successHandler(oAuth2SuccessHandler) // 로그인을 성공하면 토큰 발급
                )
                // Spring security의 기본 필터가 동작하기 전에 내가 만든 JwtAuthenticationFilter가 먼저 동작하도록 시행
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    // 프론트 연동을 위한 CORS 정책
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:5173", "https://ovc-cert.duckdns.org", "https://ovc-project.vercel.app"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true); // HttpOnly 쿠키를 주고 받으려면 반드시 true여야됨
        configuration.setExposedHeaders(List.of("Authorization")); // 프론트단에서 읽을 수 있는 헤더로 설정해준다.

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;

    }
}
