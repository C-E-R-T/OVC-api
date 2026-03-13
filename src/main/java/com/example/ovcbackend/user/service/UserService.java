package com.example.ovcbackend.user.service;

import com.example.ovcbackend.user.dto.UserResponse;

public interface UserService {
    UserResponse getMyInfo(String email);
}
