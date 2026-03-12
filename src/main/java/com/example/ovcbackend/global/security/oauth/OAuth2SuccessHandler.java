package com.example.ovcbackend.global.security.oauth;

import com.example.ovcbackend.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // oauth2User로 사용자 정보를 추출
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // customoauth2userservice에서 넘겨준 attributes 내부의 response 맵을 꺼냄
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 네이버는 response에 데이터가 있음
        String email = (String) attributes.get("email");

        // 권한 authentication을 통해 권한을 추출
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");

        //access, refresh token 생성
        String accessToken = jwtTokenProvider.createToken(email, role);
        String refreshToken = jwtTokenProvider.refreshToken(email);

        // 토큰을 HttpOnlyznzlfh vhwkd
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .path("/")
                .httpOnly(true)
                .secure(false)
                .maxAge(3600)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .path("/")
                .httpOnly(true)
                .secure(false)
                .maxAge(60 * 60 * 24 * 7) // 7일
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());


        // 백엔드에서 확인용
        String targetUrl ="http://localhost:8080/api/auth/temp-success";


        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

}
