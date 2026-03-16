package com.example.ovcbackend.certificate.dto;

import com.example.ovcbackend.certificate.entity.Certificate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CertResponse {
    @Schema(name = "자격증 이름", example = "정보처리기사")
    private String name;
    @Schema(name = "주관기관", example = "한국산업인력공단")
    private String authority;
    @Schema(name = "필기 응시료", example = "19400")
    private Integer writtenFee;
    @Schema(name = "실기 응시료", example = "19400")
    private Integer practicalFee;
    @Schema(name = "출제경향", example = "제과계량, 반죽(발효), 성형, 굽기 등의 공정을 거쳐...")
    private String examTrend;
    @Schema(name = "취득방법", example = "필기 : 객관식 4지택일형(60문항) - 실기 : 작업")
    private String acqMethod;
    @Schema(name = "시험과목", example = "필기 :과자류 재료, 제조 및 위생관리(출제기준 상세 참고)")
    private String examSubject;
    @Schema(name = "합격기준", example = "필실기 100점을 만점으로 하여 60점 이상 작업형 실기시험 기본정보 안전등급")
    private String passCriteria;
    @Schema(name = "관련학과", example = "모든 학과 응시가능")
    private String relatedDepartment;

    // 엔터티 -> dto
    public static CertResponse from(Certificate entity) {
        return CertResponse.builder()
                .name(entity.getName())
                .authority(entity.getAuthority())
                .writtenFee(entity.getWrittenFee())
                .practicalFee(entity.getPracticalFee())
                .examTrend(entity.getExamTrend())
                .acqMethod(entity.getAcqMethod())
                .examSubject(entity.getExamSubject())
                .passCriteria(entity.getPassCriteria())
                .relatedDepartment(entity.getRelatedDepartment())
                .build();
    }
}
