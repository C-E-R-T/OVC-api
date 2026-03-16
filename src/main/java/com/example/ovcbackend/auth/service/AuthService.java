package com.example.ovcbackend.auth.service;

import com.example.ovcbackend.auth.dto.*;
import com.example.ovcbackend.user.entity.User;

public interface AuthService {

    SignUpResponse signup(SignUpRequest request);

    LoginResponse login(LoginRequest request);

    void saveRefreshToken(String email, String token);

    TokenResponse refreshAccessToken(String refreshToken);

    // 로그아웃
    void logout(String email);
}
