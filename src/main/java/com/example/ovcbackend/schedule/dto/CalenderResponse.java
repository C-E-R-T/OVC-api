package com.example.ovcbackend.schedule.dto;

import com.example.ovcbackend.schedule.ExamType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "캘린더 반환 dto")
@Getter
@Builder
public class CalenderResponse {
    @Schema(description = "스케쥴 아이디", example = "1")
    private Long scheduleId;
    @Schema(description = "자격증 이름", example = "정보처리기사")
    private String certificateName;
    @Schema(description = "시험 회차 이름", example = "2026년 정기 기사 1회")
    private String examName;
    @Schema(description = "필기/실기/기타 구분", example = "필기")
    private ExamType examType;
    @Schema(description = "시험 분류", example = "APPLY")
    private String eventType;
    @Schema(description = "시험 시작일")
    private LocalDateTime startDate;
    @Schema(description = "시험 종료일")
    private LocalDateTime endDate;
    @Schema(description = "총 기간(일수)", example = "5")
    private Long durationDays;
    @Schema(description = "자격증 아이디", example = "1")
    private Long certId;
}
