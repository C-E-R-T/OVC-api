package com.example.ovcbackend.oauth;

import com.example.ovcbackend.user.Role;
import com.example.ovcbackend.user.entity.User;
import com.example.ovcbackend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
@Transactional
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException{
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        Map<String, Object> attributes = oAuth2User.getAttributes();
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");

        // 사용자 정보 매핑 (회원가입, 업데이트)
        User user = saveOrUpdate(response, registrationId);

        // security 세션에 유저 정보 저장 (권한용)
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

        User user = userRepository.findByEmail(email)
                .map(entity -> {
                    String currentNickname =(entity.getNickname() != null) ? entity.getNickname() : name;
                    return entity.update(currentNickname);
                })// 이미 있다면 업데이트
                .orElse(User.builder() // 만약에 없다면 신규로 만든다.
                        .email(email)
                        .name(name)
                        .nickname(nickname) // 네이버 닉네임과 ovc의 닉네임을 분리하기 위해 or else에 추가
                        .password(passwordEncoder.encode("OAUTH2_USER_" + UUID.randomUUID()))
                        .provider(registrationId)
                        .providerId(providerId)
                        .profileImageUrl(profileImage)
                        .role(Role.ROLE_USER)
                        .build());

        return userRepository.save(user);
    }
}
