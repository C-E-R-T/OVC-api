package com.example.ovcbackend.global.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final CustomUserDetailsService customUserDetailsService;


    public JwtTokenProvider(
            @Value("${spring.jwt.secret}") String secret,
            @Value("${spring.jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${spring.jwt.refresh-token-expiration}") long refreshTokenExpiration, CustomUserDetailsService customUserDetailsService) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.customUserDetailsService = customUserDetailsService;
        //? 테스트를 위해 10000으로 바꿨는데 왜 그전 360000으로 나오지?
        // 맨날 client_id 같은거 없다고 오류나서 local.yaml에 :로 따로 넣어줬는데
        //원인: 또 시간은 읽는다.. env 문제였음
//        System.out.println("설정된 Access 만료 시간: " + this.accessTokenExpiration);
    }

    //토큰 생성
    public String createToken(String email, String role){
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey)
                .compact();
    }

    // refresh 토큰 생성
    public String refreshToken(String email) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(validity)
                .signWith(secretKey)
                .compact();
    }

    // 토큰 사용자 추출, 이메일을 사용함으로 이메일을 추출함.
    public String getEmail(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException
                 | ExpiredJwtException | UnsupportedJwtException| IllegalArgumentException e) {
            throw e;
        }
    }

    // 인증 객체 생성
    public Authentication getAuthentication(String token) {
        String email = getEmail(token);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

        // 회원탈퇴 등으로 유저가 없을 때도 고려해야됨
        //근데 추가했는데 왜 못잡지 userdetailservice인 줄 알았는데 아니다..
        // 원인: 테스트한다고 /users를 permitAll로 풀어놨었음..

        return new UsernamePasswordAuthenticationToken(userDetails,"", userDetails.getAuthorities());
    }

    //refresh 만료 시간 localdatetime으로 변환하여 반환하는 함수 필요
    public LocalDateTime getExpirationLocalDateTime(String token) {
        Date expiration = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();

        return expiration.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }



}
