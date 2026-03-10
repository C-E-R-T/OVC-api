package com.example.ovcbackend.auth.controller;

import com.example.ovcbackend.auth.dto.LoginRequest;
import com.example.ovcbackend.auth.dto.LoginResponse;
import com.example.ovcbackend.auth.dto.SignUpRequest;
import com.example.ovcbackend.auth.dto.SignUpResponse;
import com.example.ovcbackend.auth.service.AuthService;
import com.example.ovcbackend.global.commonResponse.OkResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth API", description = "인증/인가를 관리하기 위한 api입니다.")
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
}
