package com.example.ovcbackend.auth.controller;

import com.example.ovcbackend.auth.dto.*;
import com.example.ovcbackend.auth.service.AuthService;
import com.example.ovcbackend.global.commonResponse.OkResponse;
import com.example.ovcbackend.global.util.CookieUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<OkResponse<String>> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = CookieUtils.getCookies(request, "refreshToken")
                .map(Cookie::getValue)
                .orElseThrow(() -> new RuntimeException("리프레시 토큰이 없습니다."));

        TokenResponse tokenResponse = authService.refreshAccessToken(refreshToken);

        CookieUtils.addCookie(response, "accessToken", tokenResponse.getAccessToken(), 3600);
        CookieUtils.addCookie(response, "refreshToken", tokenResponse.getRefreshToken(), 604800);

        return ResponseEntity.ok(OkResponse.success("토큰 재발급 성공", request.getRequestURI()));
    }






}
