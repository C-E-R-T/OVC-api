package com.example.ovcbackend.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpRequest {
    // 회원가입 전용 dto
    private String userName;
    private String password;
    private String email;
}
