package com.example.ovcbackend.global.security.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);

        try {
            if(token != null && jwtTokenProvider.validateToken(token)){

            }
        } catch (SecurityException | MalformedJwtException e) {
            request.setAttribute("exception", "잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            request.setAttribute("exception", "만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            request.setAttribute("exception", "지원하지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            request.setAttribute("exception", "인증 오류가 발생하였습니다.");
        }

        filterChain.doFilter(request, response);

    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }
        return null;
    }
}
