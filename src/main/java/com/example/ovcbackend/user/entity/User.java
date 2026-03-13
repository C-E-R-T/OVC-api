package com.example.ovcbackend.user.entity;

import com.example.ovcbackend.global.entity.BaseTime;
import com.example.ovcbackend.user.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter // 데이터 무결성을 위해 Setter 제거
@NoArgsConstructor(access = AccessLevel.PROTECTED)
// 클래스 레벨에 @Builder을 붙이면 모든 필드가 빌더가 포함되서 id와 basetime 필드가 노출될 수 있어 따로 구현이 좋음
public class User extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    private String nickname;

    private String password;

    private String provider;
    private String providerId;

    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.ROLE_USER;

    @Builder
    public User (String email, String name, String nickname, String password, String provider,
                 String providerId, String profileImageUrl, Role role) {
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.password = password;
        this.provider = provider;
        this.providerId = providerId;
        this.profileImageUrl = profileImageUrl;
        this.role = (role != null) ? role : Role.ROLE_USER;
    }

    public User update(String nickname, String profileImageUrl) {

        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        return this;
    }
}
