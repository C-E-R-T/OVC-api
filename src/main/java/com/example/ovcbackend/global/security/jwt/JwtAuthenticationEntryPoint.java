package com.example.ovcbackend.global.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authenticationException) throws IOException {

        // 필터에 저장한 exception(EXPIRED_ACCESS_TOKEN) 등을 가져옴
        String exceptionCode = (String) request.getAttribute("exception");

       if(exceptionCode == null) {
           exceptionCode = "UNAUTHORIZED";
       }

       // 점점 길어지네.. enum으로 빼는게 나으려나
       String message;
       if("EXPIRED_ACCESS_TOKEN".equals(exceptionCode)){
           message = "액세스 토큰이 만료되었습니다. 리프레시 토큰을 사용해 재발급하세요.";
       } else if("INVALID_TOKEN".equals(exceptionCode)) {
           message = "유효하지 않은 토큰입니다.";
       } else if("UNSUPPORTED_TOKEN".equals(exceptionCode)) {
           message = "지원하지 않는 JWT 토큰입니다.";
        } else if("ILLEGAL_TOKEN".equals(exceptionCode)){
           message = "토큰이 비거나 잘못된 인자(null)입니다.";
       } else if("USER_NOT_FOUND".equals(exceptionCode)){
           message = "유저가 없습니다.";
       }else {
           message = "인증이 필요한 서비스 입니다.";
       }

       log.warn("[JwtEntryPoint] 인증 실패 응답 발송 - Code: {}, URI: {}, Message: {}",
               exceptionCode, request.getRequestURI(), message);


        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> responseBody= new LinkedHashMap<>();
        responseBody.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        responseBody.put("error", "Unauthorized");
        responseBody.put("code", exceptionCode);
        responseBody.put("message", message);
        responseBody.put("path", request.getRequestURI());
        responseBody.put("timestamp", LocalDateTime.now().toString());

        objectMapper.writeValue(response.getWriter(), responseBody);
    }
}
