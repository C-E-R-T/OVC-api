package com.example.ovcbackend.auth.controller;

import com.example.ovcbackend.auth.dto.*;
import com.example.ovcbackend.auth.exception.TokenInvalidException;
import com.example.ovcbackend.auth.service.AuthService;
import com.example.ovcbackend.global.commonResponse.OkResponse;
import com.example.ovcbackend.oauth.util.CookieUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth controller", description = "인증/인가(auth) 관련 api")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<OkResponse<SignUpResponse>> signup (@RequestBody
    SignUpRequest signUpRequest, HttpServletRequest request){
        SignUpResponse signUpResponse = authService.signup(signUpRequest);

        return ResponseEntity.ok(OkResponse.success("회원가입이 완료되었습니다.", signUpResponse, request.getRequestURI()));
    }

    @PostMapping("/login")
    public ResponseEntity<OkResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request){
        LoginResponse loginResponse = authService.login(loginRequest);

        return ResponseEntity.ok(OkResponse.success(loginResponse, request.getRequestURI()));
    }

    @GetMapping("/refresh")
    @Operation(summary = "access token 재발급", description = "refresh token으로 새로운 access token을 재발급합니다.")
    public ResponseEntity<OkResponse<String>> refresh(HttpServletRequest request, HttpServletResponse response) {
        // 쿠키 자체로 이미 인증이 확인된 상태
        String refreshToken = CookieUtils.getCookies(request, "refreshToken")
                .map(Cookie::getValue)
                .orElseThrow(() -> new TokenInvalidException("리프레시 토큰이 없습니다."));

        TokenResponse tokenResponse = authService.refreshAccessToken(refreshToken);
        // 토큰이 새로 발급된 경우에만 쿠키를 다시 구워주기
        if(tokenResponse.getRefreshToken() != null) {
            CookieUtils.addCookie(response, "refreshToken", tokenResponse.getRefreshToken(), 604800);
        }
        // 갱신 되지 않을 경우 (null)일 경우 아무것도 하지 않음 - 기존 refreshToken 그대로 사용
//        CookieUtils.addCookie(response, "accessToken", tokenResponse.getAccessToken(), 3600);
//        CookieUtils.addCookie(response, "refreshToken", tokenResponse.getRefreshToken(), 3600);
        return ResponseEntity.ok(OkResponse.success("토큰 재발급 성공", tokenResponse.getAccessToken(), request.getRequestURI()));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "로그아웃을 진행합니다.")
    public ResponseEntity<OkResponse<Void>> logout(HttpServletRequest request, HttpServletResponse response) {
        // SecurityContext에서 현재 로그인한 유저 정보를 추출. 특히 우리는 식별자가 email임으로 email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();
            authService.logout(email);
        }

        // 브라우저에서 쿠키를 삭제함 (accessToken, refreshToken을 전부 다)
        CookieUtils.deleteCookie(request, response, "refreshToken");

        return ResponseEntity.ok(OkResponse.success("로그아웃이 완료되었습니다.", request.getRequestURI()));
        // 프론트 쪽에서는 /login쪽으로 redirect해줘야됨
    }






}
