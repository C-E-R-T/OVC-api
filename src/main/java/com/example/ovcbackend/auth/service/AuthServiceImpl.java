package com.example.ovcbackend.auth.service;

import com.example.ovcbackend.auth.dto.*;
import com.example.ovcbackend.auth.entity.RefreshToken;
import com.example.ovcbackend.auth.exception.AuthBadRequestException;
import com.example.ovcbackend.auth.exception.AuthNotFoundException;
import com.example.ovcbackend.auth.exception.TokenInvalidException;
import com.example.ovcbackend.auth.repository.RefreshTokenRepository;
import com.example.ovcbackend.global.security.jwt.JwtTokenProvider;
import com.example.ovcbackend.user.Role;
import com.example.ovcbackend.user.entity.User;
import com.example.ovcbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public SignUpResponse signup(SignUpRequest request) {
        if(userRepository.existsByEmail(request.getEmail())) {
            throw new AuthBadRequestException("이미 존재하는 이메일입니다.");
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
                .orElseThrow(() ->  new AuthNotFoundException("가입되지 않은 이메일입니다."));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AuthBadRequestException("비밀번호가 일치하지 않습니다.");
        }

        String accessToken = jwtTokenProvider.createToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenProvider.refreshToken(user.getEmail());

        LoginResponse res = LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return res;
    }

    // 로그인 성공 시 리프레시 토큰 저장
    @Override
    public void saveRefreshToken(String email, String token) {
        LocalDateTime expiresAt = jwtTokenProvider.getExpirationLocalDateTime(token);

        RefreshToken refreshToken = refreshTokenRepository.findByEmail(email)
                .map(existToken -> {
                    existToken.updateToken(token, expiresAt);
                    return existToken;
                })
                .orElse(RefreshToken.builder()
                        .email(email)
                        .token(token)
                        .expiresAt(expiresAt)
                        .build());

        refreshTokenRepository.save(refreshToken);
    }

    // 리프레시 토큰으로 액세스 토큰 발급
    @Override
    @Transactional
    public TokenResponse refreshAccessToken(String refreshToken) {
        // 토큰이 이상한 형태이면 500 error가 남.
        // 토큰이 null인 상태이거나 비었을 때도 생각해야됨
        // 특히 refresh쪽은 authentication을 필요로 하지 않기 때문에 exception을 따로 잡아줘야됨
        if(refreshToken == null || refreshToken.isBlank()) {
            throw new TokenInvalidException("리프레시 토큰이 비어있습니다.");
        }
        try {
            if(!jwtTokenProvider.validateToken(refreshToken)) {
                throw new TokenInvalidException("유효하지 않은 리프레시 토큰입니다.");
            }
        } catch (Exception e) {
            // malformedJwtException 처리
            throw new TokenInvalidException("토큰 형식이 올바르지 않습니다.");
        }

        // db에 저장된 토큰인지 확인
        RefreshToken saveRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new AuthNotFoundException("존재하지 않는 리프레시 토큰입니다."));

        User user = userRepository.findByEmail(saveRefreshToken.getEmail())
                .orElseThrow(() -> new AuthNotFoundException("사용자를 찾을 수 없습니다."));
        //새로운 토큰 발급
        String newAccessToken = jwtTokenProvider.createToken(user.getEmail(), user.getRole().name());
        String newRefreshToken = jwtTokenProvider.refreshToken(user.getEmail());

        saveRefreshToken.updateToken(newRefreshToken, jwtTokenProvider.getExpirationLocalDateTime(newRefreshToken));

        return TokenResponse.of(newAccessToken,newRefreshToken);
    }

    @Override
    @Transactional
    public void logout(String email) {
        // 로그아웃에 관한 exception은 jwtauthenticationfilter에서 처리
        refreshTokenRepository.deleteByEmail(email);
    }
}
