package com.example.ovcbackend.oauth;

import com.example.ovcbackend.user.Role;
import com.example.ovcbackend.user.entity.User;
import com.example.ovcbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

// 네이버와 서버간 STATE, CODE를 통해 본인을 확인하고 받은
// 제공자(네이버)가 발급한 Access Token을 사용하여 유저 정보를 가져와서 규격에 맞게 데이터를 정제하는 역할
//즉, JSON 구조를 프로젝트의 entity로 변환하는 과정
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional // sprint security가 기본으로 제공하는 OAuth2 유저 서비스를 상속받으므로 원하는 로직을 덧붙임
// [spring security 공식 문서]에 의하면 DefaultOAuth2UserService는 OAuth2UserService의 구현체로, 표준 OAuth2.0을 지원하며
// UserInfo 엔드 포인트에서 사용자 속성을 가져와 OAuth2User을 반환한다고 함.
// 즉 이 기본 클래스를 상속 방아서, 네이버 로그인의 Json구조 분석, db 저장등을 추가함.
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // oauth에는 password가 필요없으나 나중에 확장을 위해 무작위 번호를 암호화해 주입시킴
    // OAuth2UserRequest는 네이버 api에 접근할 수 있는 모든 정보를 들고 있음
    // registrationId, accesstoken, yaml에 설정한 client-id, client-secret의 정보를 들고 있음
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException{
        log.info("[OAuth2UserService] OAuth2 로그인 로드 시작");
        // [spring security 공식 문서] : oAuth2UserService는 액세스 토큰을 사용해 UserInfo 엔드 포인트에서 유저 정보를 가져옴
        // oauthservice단의 laoduser을 이욯해서 user 정보를 가져옴 또한 Header에 Bearer{token}을 알아서 넣어줌
        //defaultOAuth2UserService의 기능을 통해 user-info-uri로 요청을 보냄
        // 그래서 super.loadUser(userRequest)를 보낸 순간 spring security가 내부적으로 RestTemplate을 사용해
        // 트큰을 줄테니 유저 정보를 달라고 요청을 보내고 그 결과가 oAuth2User 담김
        // user-info-uri에서 보내준 유저 데이터(Json)을 받아옴
        OAuth2User oAuth2User = super.loadUser(userRequest); // 부모 클래스의 기능을 빌려 네이버 서버로 부터 유저의 원본 정보를 가져옴.
        log.info("[OAuth2UserService] 네이버 서비스로부터 유저 원본 데이터 획득 성공");
        // 어떤 서비스인지 구분하는 방법
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("[OAuth2UserService] 서비스 제공자: {}", registrationId);

        // [spring security 공식 문서] : 응답으로 AuthenticatiedPrincipal을 OAuth2User 형태로 반환하며, 내부적으로
        // 유저의 속성(Attributes)를 가지고 있다.
        // 하지만 네이번느 표준과 다르게 네이버 개발자 센터의 공식 문서를 확인한 결과 유저 정보를 response라는 키안에 한 번 더 감싸서 보내서
        // response안에 든 객체를 프로젝트에 맞게 가공함.
        Map<String, Object> attributes = oAuth2User.getAttributes();
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        // 사용자 정보 매핑 (회원가입, 업데이트)
        User user = saveOrUpdate(response, registrationId);

        log.info("[OAuth2UserService] 인증 처리 완료 Email: {}", user.getEmail());
        // security 유저 정보 저장 (권한용)
        // 즉 spring security에게 role, 상세 정보, id를 알려주는 것
        // DefaultOauth2User객체는 자동으로 SecurityContextHolder에 저장되서, 애플리케이션 어디는 현재 로그인한 유저
        //정보를 꺼내 쓸 수 있음.
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority(user.getRole().name())),
                response, "id"
        );


    }

    private User saveOrUpdate(Map<String, Object> response, String registrationId) {
        String email = (String) response.get("email");
        String name = (String) response.get("name");
        String nickname = (String) response.get("nickname");
        String profileImage = (String) response.get("profile_image");
        String providerId = (String) response.get("id");

        log.info("[OAuth2UserService] DB 저장/업데이트 시도 - Email: {}", email);
        User user = userRepository.findByEmail(email)
                .map(entity -> {
                    log.info("[OAuth2UserService] 기존 회원 정보 업데이트 수행: {}", email);
                    String currentNickname =(entity.getNickname() != null) ? entity.getNickname() : name;
                    return entity.update(currentNickname);
                })// 이미 있다면 업데이트
                .orElseGet(() -> {
                        log.info("[OAuth2UserService] 신규 회원 가입 진행: {}", email);
                        return User.builder() // 만약에 없다면 신규로 만든다.
                            .email(email)
                            .name(name)
                            .nickname(nickname) // 네이버 닉네임과 ovc의 닉네임을 분리하기 위해 or else에 추가
                            .password(passwordEncoder.encode("OAUTH2_USER_" + UUID.randomUUID()))
                            .provider(registrationId)
                            .providerId(providerId)
                            .profileImageUrl(profileImage)
                            .role(Role.ROLE_USER)
                            .build();
                });

        return userRepository.save(user);
    }
}

// 즉 [spring security 공식 문서] : "기본 기능을 쓰고 싶으면 DefaultOAuth2UserService" 사용을 권장
// 내 코드: 표준 규격에 맞추되 이것을 상속받아서 (super.loadUser)을 통해 네이버 데이터만 우리 DB 형싱에 맞게 설정함.
// 이 과정은 네이버 서버와 통신해서 유저 정보를 텍스트(JSON)으로 받아와서 자바 객체로 변환해주는 것
// DefaultOAuth2UserService를 쓰지 않으면 직접 RestTemplate이나 WebClient를 선언하고, 토큰을 헤더에 넣고, 예외를
// 처리하고 Json 하나를 파싱해야됨. 네이버 개발자센터가 제공했던 예시 처럼.