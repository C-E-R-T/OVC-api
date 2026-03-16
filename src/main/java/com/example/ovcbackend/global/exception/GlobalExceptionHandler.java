package com.example.ovcbackend.global.exception;

import com.example.ovcbackend.auth.exception.AuthBadRequestException;
import com.example.ovcbackend.auth.exception.AuthNotFoundException;
import com.example.ovcbackend.auth.exception.TokenInvalidException;
import com.example.ovcbackend.certificate.exception.CertNotFoundException;
import com.example.ovcbackend.schedule.exception.ScheduleNotFoundException;
import com.example.ovcbackend.user.exception.UserConflictException;
import com.example.ovcbackend.user.exception.UserNotFoundException;
import com.example.ovcbackend.user.favorite.exception.FavoriteBadRequestException;
import com.example.ovcbackend.user.favorite.exception.FavoriteConflictException;
import com.example.ovcbackend.user.favorite.exception.FavoriteNotFoundException;
import com.example.ovcbackend.user.cert.exception.MyCertBadRequestException;
import com.example.ovcbackend.user.cert.exception.MyCertConflictException;
import com.example.ovcbackend.user.cert.exception.MyCertNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FavoriteBadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(FavoriteBadRequestException e, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage(), request);
    }

    @ExceptionHandler(MyCertBadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleMyCertBadRequest(MyCertBadRequestException e, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage(), request);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<Map<String, Object>> handleValidation(Exception e, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "요청 파라미터 형식이 올바르지 않습니다.", request);
    }

    @ExceptionHandler(FavoriteNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(FavoriteNotFoundException e, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage(), request);
    }

    @ExceptionHandler(MyCertNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleMyCertNotFound(MyCertNotFoundException e, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage(), request);
    }

    @ExceptionHandler(FavoriteConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(FavoriteConflictException e, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, e.getMessage(), request);
    }

    @ExceptionHandler(MyCertConflictException.class)
    public ResponseEntity<Map<String, Object>> handleMyCertConflict(MyCertConflictException e, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, e.getMessage(), request);
    }

    // Auth단의 badrequest
    @ExceptionHandler(AuthBadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleAuthBadRequest(AuthBadRequestException e, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage(), request);
    }

    @ExceptionHandler(AuthNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleAuthNotFound(AuthNotFoundException e, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage(), request);
    }

    @ExceptionHandler(TokenInvalidException.class)
    public ResponseEntity<Map<String, Object>> handleTokenInvalid(TokenInvalidException e, HttpServletRequest request) {
        return  buildErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage(), request);
    }
    // 이쪽 globaleception도 시간이 가능하다면 Enum 하나 만들어주는게 좋을 듯하다..

    @ExceptionHandler(CertNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCertNotFound(CertNotFoundException e, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage(), request);
    }

    @ExceptionHandler(ScheduleNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleScheduleNotFound(ScheduleNotFoundException e, HttpServletRequest request) {
        return  buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage(), request);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException e, HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage(), request);
    }

    @ExceptionHandler(UserConflictException.class)
    public ResponseEntity<Map<String, Object>> handleUserConflict(UserConflictException e, HttpServletRequest request) {
        return  buildErrorResponse(HttpStatus.CONFLICT, e.getMessage(),request);
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message,
                                                                   HttpServletRequest request) {
        // 프론트에서 공통 처리하기 쉬운 형태로 에러 바디를 통일
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", request.getRequestURI());
        body.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.status(status).body(body);
    }
}
