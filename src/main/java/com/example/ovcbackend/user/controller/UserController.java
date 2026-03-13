package com.example.ovcbackend.user.controller;

import com.example.ovcbackend.global.commonResponse.OkResponse;
import com.example.ovcbackend.user.dto.UserResponse;
import com.example.ovcbackend.user.dto.UserUpdateRequest;
import com.example.ovcbackend.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<OkResponse<UserResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request
            ) {
        String email = userDetails.getUsername();

        UserResponse response = userService.getMyInfo(email);

        return ResponseEntity.ok(OkResponse.success(response, request.getRequestURI()));
    }

    @PatchMapping("/me")
    public ResponseEntity<OkResponse<UserResponse>> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserUpdateRequest userUpdateRequest,
            HttpServletRequest request
            ) {
        String email = userDetails.getUsername();

        UserResponse response = userService.updateMyInfo(email, userUpdateRequest);

        return ResponseEntity.ok(OkResponse.success(response, request.getRequestURI()));
    }
}
