package com.example.ovcbackend.user.controller;

import com.example.ovcbackend.global.commonResponse.OkResponse;
import com.example.ovcbackend.oauth.util.CookieUtils;
import com.example.ovcbackend.user.dto.UserResponse;
import com.example.ovcbackend.user.dto.UserUpdateRequest;
import com.example.ovcbackend.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User controller", description = "user 관련 api")
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
        if (userDetails == null) {
            throw new RuntimeException("인증 정보가 없습니다. 다시 로그인해주세요.");
        }

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


    // 회원탈퇴 시 refresh토큰, 쿠키, 유저 데이터 삭제
    @DeleteMapping("/me")
    public ResponseEntity<OkResponse<Void>> deleteUser(HttpServletRequest request, HttpServletResponse response) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // db에서 유저 데이터 삭제
        if(auth != null && auth.isAuthenticated()) {
            userService.deleteUser(auth.getName());
        }
        // 쿠키 삭제
        CookieUtils.deleteCookie(request, response, "accessToken");
        CookieUtils.deleteCookie(request, response, "refreshToken");

        return ResponseEntity.ok(OkResponse.success("회원탈퇴가 완료되었습니다.", request.getRequestURI()));
    }
}
