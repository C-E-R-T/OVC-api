package com.example.ovcbackend.schedule.dto;

import com.example.ovcbackend.schedule.ExamType;
import com.example.ovcbackend.schedule.entity.Schedule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ScheduleResponse {
    private String examName;
    private String examRound;
    private ExamType examType;
    private LocalDateTime applyStartAt;
    private LocalDateTime applyEndAt;
    private LocalDateTime examStartAt;
    private LocalDateTime examEndAt;
    private LocalDateTime resultAt;

    public static ScheduleResponse from (Schedule s) {
        return ScheduleResponse.builder()
                .examName(s.getExamName())
                .examRound(s.getExamRound())
                .examType(s.getExamType())
                .applyStartAt(s.getApplyStartAt())
                .applyEndAt(s.getApplyEndAt())
                .examStartAt(s.getExamStartAt())
                .examEndAt(s.getExamEndAt())
                .resultAt(s.getResultAt())
                .build();
    }


}
