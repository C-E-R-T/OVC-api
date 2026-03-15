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
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // oauth에는 password가 필요없으나 나중에 확장을 위해 무작위 번호를 암호화해 주입시킴
    // OAuth2UserRequest는 네이버 api에 접근할 수 있는 모든 정보를 들고 있음
    // registrationId, accesstoken, yaml에 설정한 client-id, client-secret의 정보를 들고 있음
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException{
        log.info("[OAuth2UserService] OAuth2 로그인 로드 시작");
        // oauthservice단의 laoduser을 이욯해서 user 정보를 가져옴 또한 Header에 Bearer{token}을 알아서 넣어줌
        //defaultOAuth2UserService의 기능을 통해 user-info-uri로 요청을 보냄
        // user-info-uri에서 보내준 유저 데이터(Json)을 받아옴
        OAuth2User oAuth2User = super.loadUser(userRequest); // 부모 클래스의 기능을 빌려 네이버 서버로 부터 유저의 원본 정보를 가져옴.
        log.info("[OAuth2UserService] 네이버 서비스로부터 유저 원본 데이터 획득 성공");
        // 어떤 서비스인지 구분하는 방법
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("[OAuth2UserService] 서비스 제공자: {}", registrationId);

        Map<String, Object> attributes = oAuth2User.getAttributes();
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        // 사용자 정보 매핑 (회원가입, 업데이트)
        User user = saveOrUpdate(response, registrationId);

        log.info("[OAuth2UserService] 인증 처리 완료 Email: {}", user.getEmail());
        // security 유저 정보 저장 (권한용)
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
