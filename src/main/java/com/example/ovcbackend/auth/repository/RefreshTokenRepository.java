package com.example.ovcbackend.auth.repository;

import com.example.ovcbackend.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByEmail(String email);

    Optional<RefreshToken> findByToken(String token);
}
