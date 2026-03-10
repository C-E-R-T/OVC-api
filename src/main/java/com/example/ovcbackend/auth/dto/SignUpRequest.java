package com.example.ovcbackend.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequest {
    // 회원가입 전용 dto
    @Schema(name = "username", example = "user")
    private String userName;
    @Schema(name = "password", example = "12345678")
    private String password;
    @Schema(name = "email", example = "user@example.com")
    private String email;
}
