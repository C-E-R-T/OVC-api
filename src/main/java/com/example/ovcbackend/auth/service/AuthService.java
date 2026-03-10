package com.example.ovcbackend.auth.service;

import com.example.ovcbackend.auth.dto.LoginRequest;
import com.example.ovcbackend.auth.dto.LoginResponse;
import com.example.ovcbackend.auth.dto.SignUpRequest;
import com.example.ovcbackend.auth.dto.SignUpResponse;
import com.example.ovcbackend.user.entity.User;

public interface AuthService {

    SignUpResponse signup(SignUpRequest request);

    LoginResponse login(LoginRequest request);

}
