package com.example.ovcbackend.user.cert.entity;

import com.example.ovcbackend.certificate.entity.Certificate;
import com.example.ovcbackend.global.entity.BaseTime;
import com.example.ovcbackend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

@Entity
@Table(
        name = "user_certificates",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_user_certificates_user_cert",
                        columnNames = {"user_id", "cert_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCertificate extends BaseTime {

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

    @Column(name = "cert_number", length = 100)
    private String certNumber;

    @Column(name = "passed_at")
    private LocalDate passedAt;

    @Column(name = "expired_at")
    private LocalDate expiredAt;

    @Builder
    public UserCertificate(User user, Certificate certificate, String certNumber, LocalDate passedAt, LocalDate expiredAt) {
        this.user = user;
        this.certificate = certificate;
        this.certNumber = certNumber;
        this.passedAt = passedAt;
        this.expiredAt = expiredAt;
    }
}
