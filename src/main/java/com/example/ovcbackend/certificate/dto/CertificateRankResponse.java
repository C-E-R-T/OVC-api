package com.example.ovcbackend.certificate.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CertificateRankResponse {
    private Long certId;
    private String name;
    private long likeCount;
}
