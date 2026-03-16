package com.example.ovcbackend.global.security.jwt;

import com.example.ovcbackend.oauth.util.CookieUtils;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// securityconfig에 주입하기 위해 component를 붙여야 bean으로 등록됨.
// security config에서 2번 실행될 수 있어서 component 제거

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);
        String requestURI = request.getRequestURI();

        try {
            if(token != null && jwtTokenProvider.validateToken(token)){
                // 토큰으로 유저 정보를 담은 authentication 가져오기
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                // security context에 인증 정보 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.info("[JwtFilter] 인증 성공 - User: {}, URI: {}", authentication.getName(),requestURI);
            }
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("[JwtFilter] 유효하지 않은 토큰 - URI: {}, Message: {}", requestURI, e.getMessage());
            request.setAttribute("exception", "INVALID_TOKEN" );
        } catch (ExpiredJwtException e) {
            log.warn("[JwtFilter] 만료된 토큰 - URI: {}, Message: {}", requestURI, e.getMessage());
            request.setAttribute("exception", "EXPIRED_ACCESS_TOKEN");
        } catch (UnsupportedJwtException e) {
            log.warn("[JwtFilter] 지원하지 않는 토큰 - URI: {}, Message: {}", requestURI, e.getMessage());
            request.setAttribute("exception", "UNSUPPORTED_TOKEN");
        } catch (IllegalArgumentException e) {
            log.warn("[JwtFilter] 잘못된 토큰 - URI: {}, Message: {}", requestURI, e.getMessage());
            request.setAttribute("exception", "ILLEGAL_TOKEN");
        } catch (UsernameNotFoundException e){
            log.warn("[JwtFilter] 사용자를 찾을 수 없음 - URI: {}, Message: {}", requestURI, e.getMessage());
            request.setAttribute("exception", "USER_NOT_FOUND");
        }
        catch (Exception e) {
            log.warn("[JwtFilter] 알 수 없는 인증 에러 - URI: {}, Message: {}", requestURI, e.getMessage());
            request.setAttribute("exception", "UNKNOWN_ERROR");
        }

        filterChain.doFilter(request, response);

    }

    private String resolveToken(HttpServletRequest request) {
        // 헤더에서 authorization: Bearer <Token> 형식으로 오는거 먼저 확인
        String bearerToken = request.getHeader("Authorization");
        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }

        // 헤더가 없으면 쿠키를 확인
        return CookieUtils.getCookies(request, "accessToken")
                .map(Cookie::getValue)
                .orElse(null);
        // 쿠키
//        Cookie[] cookies = request.getCookies();
//        if(cookies != null) {
//            for (Cookie cookie : cookies) {
//                if("accessToken".equals(cookie.getName())) {
//                    return cookie.getValue();
//                }
//            }
//        }
//        return null;
    }
}
