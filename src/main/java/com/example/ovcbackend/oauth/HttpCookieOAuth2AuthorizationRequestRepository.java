package com.example.ovcbackend.oauth;

import com.example.ovcbackend.oauth.util.CookieUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

// CookieUtils를 사용해서 spring security가 세션 대신 쿠키에 로그인 요청 정보를 저장하게 만듬
@Slf4j
@Component // Spring Bean으로 동록해 SecurityConfig에서 불러와 사용할 수 있게 함
public class HttpCookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    // [spring security 문서]: responsible for the persistance of the OAuth2Authorization
    // 세션 대신 쿠키를 통해 인증 요청 정보를 유지함
    // 정보를 저장할 쿠키의 이름
    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    // 쿠키의 유효 기간
    private static final int COOKIE_EXPIRE_SECONDS = 180;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        //[spring security 문서]: used to correlate and validate the authorization response
        // 네이버로부터 돌아온 응답(callback)이 내가 보냈던 요청이 맞는지 확인하기 위해
        // 쿠키에 기존 요청 정보를 읽어와 대조(correlate)할 준비를 함
        // HttpServletRequest 속에 저장한 쿠키가 있는지 확인
        log.info("[OAuth2CookieRepo] 인증 요청 정보 조회 시도");
        // oauth2_auth_request인 쿠키를 찾음
        return CookieUtils.getCookies(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
                .map(cookie -> {
                    log.info("[OAuth2CookieRepo] 인증 요청 쿠키 발견: {}", OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
                    // 쿠키는 문자열 데이터기 때문에 자바 객체인 OAuth2AuthorizationRequest로 변환(역직렬화)해서 가져옴.
                    return CookieUtils.deserialize(cookie, OAuth2AuthorizationRequest.class);
                })
                .orElseGet(() -> {
                    log.info("[OAuth2CookieRepo] 저장된 인증 요청 쿠키가 없습니다.");
                    return null;
                });
    }

    // 쿠키에 정보 저장하기
    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                         HttpServletRequest request,
                                         HttpServletResponse response) {
        //[spring security 문서]: form the time the authorization request is initiated
        // 적용: 사용자가 네이버 로그인 버튼을 눌러 요청이 시작되는 시점에 보안을 위해 임지 정보를 쿠키에 저장함
        // 만약 저장할 정보가 비어있다면, 기존에 남아있을지 모를 쿠키를 삭제
        if(authorizationRequest == null) {
            log.info("[OAuth2CookieRepo] 전달된 요청이 비어있어 인증 쿠키를 삭제합니다.");
            CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
            return;
        }
        log.info("[OAuth2CookieRepo] 인증 요청 정보를 쿠키에 저장합니다. (유효시간: {}초)", COOKIE_EXPIRE_SECONDS);
        // HttpServletResponse에 쿠키 추가
        CookieUtils.addCookie(response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                CookieUtils.serialize(authorizationRequest), COOKIE_EXPIRE_SECONDS);

    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        //[]
        log.info("[OAuth2CookieRepo] 인증 요청 정보 제거를 위해 조회를 수행합니다.");
        return this.loadAuthorizationRequest(request);
    }

    public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        log.info("[OAuth2CookieRepo] OAuth2 프로세스 완료 - 모든 임시 인증 쿠키 삭제");
        CookieUtils.deleteCookie(request, response, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
    }
}
