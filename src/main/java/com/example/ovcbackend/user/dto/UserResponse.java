package com.example.ovcbackend.user.dto;

import com.example.ovcbackend.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private String profileImageUrl;
    private String role;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .role(user.getRole().name())
                .build();
    }
}
