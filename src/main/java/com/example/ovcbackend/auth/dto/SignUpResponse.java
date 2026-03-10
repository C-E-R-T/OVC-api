package com.example.ovcbackend.auth.dto;

import com.example.ovcbackend.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SignUpResponse {
    @Schema(name = "email", example = "user@example.com")
    private String email;
    @Schema(name= "name", example = "user")
    private String name;

    public static SignUpResponse from(User user) {
        return SignUpResponse.builder()
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }
}
