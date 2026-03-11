package com.example.ovcbackend.global.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authenticationException) throws IOException {

        String message = (String) request.getAttribute("exception");
        if(message == null){
            message = "인증이 필요하거나 유효하지 않는 토큰입니다.";
        }

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        Map<String, Object> responseBody= new HashMap<>();
        responseBody.put("success", false);
        responseBody.put("error", "Unauthorized");
        responseBody.put("message", message);
        responseBody.put("path", request.getRequestURI());
        responseBody.put("timestamp", LocalDateTime.now().toString());

        objectMapper.writeValue(response.getWriter(), responseBody);
    }
}
