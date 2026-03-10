package com.example.ovcbackend.auth.dto;

import com.example.ovcbackend.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SignUpResponse {
    private String email;
    private String name;

    public static SignUpResponse from(User user) {
        return SignUpResponse.builder()
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }
}
