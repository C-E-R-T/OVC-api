package com.example.ovcbackend.auth.entity;

import com.example.ovcbackend.global.entity.BaseTime;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "refresh_tokens")
public class RefreshToken extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Builder
    public RefreshToken(String email, String token, LocalDateTime expiresAt){
        this.email = email;
        this.token = token;
        this.expiresAt = expiresAt;
    }

}
