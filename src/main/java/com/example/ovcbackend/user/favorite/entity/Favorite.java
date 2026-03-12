package com.example.ovcbackend.user.favorite.entity;

import com.example.ovcbackend.certificate.entity.Certificate;
import com.example.ovcbackend.global.entity.BaseTime;
import com.example.ovcbackend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
        name = "favorites",
        // 동일 유저가 동일 자격증을 중복 찜하지 못하도록 보장
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_user_certificate",
                        columnNames = {"user_id", "cert_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Favorite extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cert_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Certificate certificate;

    @Builder
    public Favorite(User user, Certificate certificate) {
        this.user = user;
        this.certificate = certificate;
    }
}
