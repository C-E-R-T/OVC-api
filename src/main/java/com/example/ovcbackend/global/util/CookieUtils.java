package com.example.ovcbackend.global.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import java.io.*;
import java.util.Base64;
import java.util.Optional;
// sesstion stateless를 위한 cookie 설정 util

// 이 util은 로그인 요청이 오면 직렬화해서 사용자의 브라우저에 주고
// 사용자가 네이버 로그인을 마치고 돌아오면 자기 한테있던 쿠키를 다시 서버에 반환
// 그래서 서버는 이 때 받은 쿠키를 역직렬화함으로써 아까 로그인을 요청했던 사람을 누구인지 알 수 있음
// 서버는 그래서 로그인 요청이 동시에 와도 메모리를 전혀 쓰지 않는 stateless한 상태로 유지할 수 있게 됨.
public class CookieUtils {

    // 쿠키를 가져옴
    public static Optional<Cookie> getCookies(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if(cookies != null && cookies.length > 0) {
            for(Cookie cookie : cookies) {
                if(cookie.getName().equals(name)) {
                    return Optional.of(cookie);
                }
            }
        }
        return Optional.empty();
    }

    // 쿠키 생성
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .path("/")
                .httpOnly(true)
                .secure(false)
                .maxAge(maxAge)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // 쿠키 삭제
    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    ResponseCookie deleteCookie = ResponseCookie.from(name, "")
                            .path("/")
                            .maxAge(0)
                            .httpOnly(true)
                            .secure(false)
                            .sameSite("Lax")
                            .build();
                    response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
                }
            }
        }
    }

    // 객체를 문자열로 직렬화 // 쿠키에 담기 좋게 변환하기 위해
    // oauth2authorizationrequest는 복잡한 자바 객체인데 쿠키에 넣으려면 한 줄로 즉 문자열로 반환해야됨.
    // 쿠키는 문자열만 담을 수 있기 때문에.
    // 그래서 객체를 문자열로 직렬화 해서 쿠키를 만듬
    public static String serialize (Object object) {
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)){
            objectOutputStream.writeObject(object);
            return Base64.getUrlEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            throw  new RuntimeException("직렬화 실패", e);
        }
    }

    // 근데 이제 브라우저가 쿠키를 돌려줄 때 백엔드 단에선는 문자열이 었던 쿠키를 객체로 조립해야 사용이 가능해짐
    // 그래서 자바 객체로 조립하는 객체가 필요함
    public static <T> T deserialize(Cookie cookie, Class<T> tClass) {
        byte[] data = Base64.getUrlDecoder().decode(cookie.getValue());
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        ) {
            return tClass.cast(objectInputStream.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("역직렬화 실패", e);
        }
    }
}
