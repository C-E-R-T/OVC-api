package com.example.ovcbackend.user.cert.dto;

import com.example.ovcbackend.certificate.entity.Certificate;
import com.example.ovcbackend.user.cert.entity.UserCertificate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
@Schema(description = "내 자격증 목록 응답 dto")
public class MyCertResponse {
    private Long certId;
    private String name;
    private String authority;
    private String certNumber;
    private LocalDate passedAt;
    private LocalDate expiredAt;

    public static MyCertResponse from(UserCertificate userCertificate) {
        Certificate certificate = userCertificate.getCertificate();

        return MyCertResponse.builder()
                .certId(certificate.getId())
                .name(certificate.getName())
                .authority(certificate.getAuthority())
                .certNumber(userCertificate.getCertNumber())
                .passedAt(userCertificate.getPassedAt())
                .expiredAt(userCertificate.getExpiredAt())
                .build();
    }
}
