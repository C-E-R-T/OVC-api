package com.example.ovcbackend.oauth;

import com.example.ovcbackend.auth.service.AuthService;
import com.example.ovcbackend.global.security.jwt.JwtTokenProvider;
import com.example.ovcbackend.oauth.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

// 로그인이 끝난 뒤 우리 서버의 JWT를 발행해 마이페이지로 보내는 역할
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;
    // 로그인 과정 중에 브라우저 쿠키에 임시골 저정했던 인증 요청 정보(state)를 관리하거나 삭제할 수 있게해줌
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    //네이버 로그인 성공 시, 이제 서버가 자체적으로 통제권을 쥐고 서비스읭 인증 환경을 구축하는 연결 다리
    // 즉, 네이버 로그인 이후 네이버가 준 정보를 활용해 우리 서비스 인증 시스템(JWT)으로 교환하는 자바 클래스
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // oauth2User로 사용자 정보를 추출
        // 즉 authentication.getPrincipal()로 인증된 유저 객체를 반환하고 이를 OAuth2용 유저 객체인 OAuth2User로 만들어
        // 유저 정보에 접근할 준비
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        //
        // customoauth2userservice에서 넘겨준 attributes 내부의 response 맵을 꺼냄
        Map<String, Object> attributes = oAuth2User.getAttributes();
        // 네이버는 response에 데이터가 있음 그리고 그 중 우리는 이메일을 가지고 사용자를 식별함으로 네이버 유저 정보에서
        //이메일을 추출함.
        String email = (String) attributes.get("email");
        log.info("[OAuth2Success] 네이버 로그인 성공 - Email: {}", email);
        // 우리 서비스에 대한 점권 권한을 우리의 토큰으로 하기로 했으므로 이 아래부터는 우리만의 토큰 발행이 필요해짐
        //즉 네이버의 토큰은 유저를 인증하고 네이버로 부터 유저의 정보를 가져와주는 역할
        // 우리 서비스에 접근할 수 있는 토큰이 아님


        // 권한 authentication을 통해 권한을 추출
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ROLE_USER");

        //우리 서비스만의 access, refresh token 생성
        // access Token은 유저 식별값인 email과 권한을 담음
        String accessToken = jwtTokenProvider.createToken(email, role);
        String refreshToken = jwtTokenProvider.refreshToken(email);

        log.info("[OAuth2Success] 서비스 전용 토큰 발행 완료 - Role: {}", role);
        // db에 refresh token을 저장(access token이 만료 시 재발급을 위해. email은 대조해보기 위해)
        authService.saveRefreshToken(email, refreshToken);
        log.info("[OAuth2Success] refreshToken DB 저장 완료");
        // 서버가 만든 JWT들을 브라우저의 쿠키에 저장
//        CookieUtils.addCookie(response, "accessToken", accessToken, 604800);
        CookieUtils.addCookie(response, "refreshToken", refreshToken, 604800);
        log.info("[OAuth2Success] 인증 쿠키 설정 완료");
        // 백엔드에서 확인용 // 프론트엔드 리다이렉트 url
        // 로그인 처리가 다 끝난 뒤에 사용자를 보낼 프론트엔드의 주소
        String targetUrl ="http://localhost:5173/mypage?accessToken=" + accessToken;

        // 로그인에 성공하면, 로그인 과정 중에 생성된 불필요한 임시 데이터를 지우기
        clearAuthenticationAttributes(request, response);
        log.info("[OAuth2Success] 모든 처리 완료. 마이페이지로 이동: {}", targetUrl);
        // 실제 브라우저를 설정한 targetUrl로 이동시킴. 이 때 브라우저는 응답 헤더에 담긴 쿠키를 함께 저장해서 이동
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        // Spring security가 기본적으로 세션에 저장하는 에러 메시지나 인증 데이터를 삭제
        super.clearAuthenticationAttributes(request);
        // 세션을 사용하지 않고 쿠키를 사용해 인증 요청을 처리할 것임으로 로그인이 끝났으면 그동안 브라우저에 저장하던 로그인 중에 사용된 임시 쿠키들을 모두 지워즘
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
        log.info("[OAuth2Success] OAuth2 로그인 임시 쿠키 삭제 완료");

    }

}
