package com.example.ovcbackend.user.service;

import com.example.ovcbackend.user.dto.UserResponse;
import com.example.ovcbackend.user.dto.UserUpdateRequest;

public interface UserService {
    UserResponse getMyInfo(String email);

    UserResponse updateMyInfo(String email, UserUpdateRequest userUpdateRequest);

    void deleteUser(String email);
}
