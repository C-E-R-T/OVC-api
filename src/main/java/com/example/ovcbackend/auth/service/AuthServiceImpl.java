package com.example.ovcbackend.auth.service;

import com.example.ovcbackend.auth.dto.LoginRequest;
import com.example.ovcbackend.auth.dto.LoginResponse;
import com.example.ovcbackend.auth.dto.SignUpRequest;
import com.example.ovcbackend.auth.dto.SignUpResponse;
import com.example.ovcbackend.global.security.jwt.JwtTokenProvider;
import com.example.ovcbackend.user.Role;
import com.example.ovcbackend.user.entity.User;
import com.example.ovcbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public SignUpResponse signup(SignUpRequest request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }


        // 패스워드 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder().email(request.getEmail())
                .name(request.getUserName())
                .password(encodedPassword)
                .role(Role.ROLE_USER)
                .build();

        // user db 저장 및 반환하기 jpa는 save로 insert
        User savedUser = userRepository.save(user);

        return SignUpResponse.from(savedUser);
    }

    @Override
    public LoginResponse login(LoginRequest request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->  new RuntimeException("가입되지 않은 이메일입니다."));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenProvider.RefreshToken(user.getEmail());

        LoginResponse res = LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return res;
    }
}
