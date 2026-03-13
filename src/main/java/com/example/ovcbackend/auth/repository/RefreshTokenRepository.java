package com.example.ovcbackend.auth.repository;

import com.example.ovcbackend.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByEmail(String email);
    // 토큰 재발급 요청
    Optional<RefreshToken> findByToken(String token);
}
