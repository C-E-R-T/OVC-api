package com.example.ovcbackend.certificate.dto;

import com.example.ovcbackend.certificate.entity.Certificate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CertResponse {
    private String name;
    private String authority;
    private Integer writtenFee;
    private Integer practicalFee;
    private String examTrend;
    private String acqMethod;
    private String precautions;

    // 엔터티 -> dto
    public static CertResponse from(Certificate entity) {
        return CertResponse.builder()
                .name(entity.getName())
                .authority(entity.getAuthority())
                .writtenFee(entity.getWrittenFee())
                .practicalFee(entity.getPracticalFee())
                .examTrend(entity.getExamTrend())
                .acqMethod(entity.getAcqMethod())
                .precautions(entity.getPrecautions())
                .build();
    }
}
