package com.example.ovcbackend.global.commonResponse;

import java.time.LocalDateTime;

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
}
