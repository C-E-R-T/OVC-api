package com.example.ovcbackend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class LoginRequest {
    @Schema(name = "email", example = "user@example.com")
    private String email;
    @Schema(name = "password", example = "1234")
    private String password;
}
