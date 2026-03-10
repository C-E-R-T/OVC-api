package com.example.ovcbackend.global.commonResponse;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
public class OkResponse<T>{
    private int status;

    private String message;

    private T data;

    private String timestamp;

    private String path;

    public OkResponse(int status, String message, T data, String path){
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now().toString();
        this.path = path;
    }

    // 데이터 값이 없는 경우의 response
    public static <T> OkResponse<T> success(String message, String path) {
        return new OkResponse<>(200, message, null, path);
    }

    public static <T> OkResponse<T> success(T data, String path){
        return new OkResponse<>(200, "요청 성공", data, path);
    }

    public static <T> OkResponse<T> success(String message, T data, String path){
        return new OkResponse<>(200, message, data, path);
    }

    public static <T> OkResponse<Map<String, Object>> successPage(Page<T> page, String path) {
        // 프론트엔드가 딱 필요로 하는 핵심 페이징 정보만 모으기
        Map<String, Object> pageData = new LinkedHashMap<>();
        pageData.put("content", page.getContent());          // 실제 데이터 리스트
        pageData.put("totalElements", page.getTotalElements()); // 전체 개수
        pageData.put("totalPages", page.getTotalPages());    // 전체 페이지 수
        pageData.put("currentPage", page.getNumber());       // 현재 페이지 (0부터)
        pageData.put("size", page.getSize());                // 한 페이지 크기
        pageData.put("isFirst", page.isFirst());             // 첫 페이지 여부
        pageData.put("isLast", page.isLast());               // 마지막 페이지 여부

        return new OkResponse<>(200, "요청 성공", pageData, path);
    }
}
