package com.example.ovcbackend.schedule.entity;

import com.example.ovcbackend.certificate.entity.Certificate;
import com.example.ovcbackend.global.entity.BaseTime;
import com.example.ovcbackend.schedule.ExamType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule extends BaseTime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exam_name", length = 100, nullable = false)
    private String examName;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", length = 50)
    private ExamType examType; //필기,실기

    @Column(name = "exam_round")
    private String examRound;

    @Column(name="apply_start_at", nullable = false)
    private LocalDateTime applyStartAt;

    @Column(name = "apply_end_at", nullable = false)
    private LocalDateTime applyEndAt;

    @Column(name="exam_start_at", nullable = false)
    private LocalDateTime examStartAt;

    @Column(name = "exam_end_at", nullable = false)
    private LocalDateTime examEndAt;

    @Column(name = "result_at", nullable = false)
    private LocalDateTime resultAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cert_id", nullable = false)
    private Certificate certificate;

    @Builder
    public Schedule (
            String examName,
            String examRound,
            ExamType examType,
            LocalDateTime applyStartAt,
            LocalDateTime applyEndAt,
            LocalDateTime examStartAt,
            LocalDateTime examEndAt,
            LocalDateTime resultAt,
            Certificate certificate
    ){
        this.examName = examName;
        this.examRound = examRound;
        this.examType = examType;
        this.applyStartAt = applyStartAt;
        this.applyEndAt = applyEndAt;
        this.examStartAt = examStartAt;
        this.examEndAt = examEndAt;
        this.resultAt = resultAt;
        this.certificate = certificate;
    }

    public void updateSchedule(
            String examName,
            String examRound,
            ExamType examType,
            LocalDateTime applyStartAt,
            LocalDateTime applyEndAt,
            LocalDateTime examStartAt,
            LocalDateTime examEndAt,
            LocalDateTime resultAt
    ) {
        this.examName = examName;
        this.examRound = examRound;
        this.examType = examType;
        this.applyStartAt = applyStartAt;
        this.applyEndAt = applyEndAt;
        this.examStartAt = examStartAt;
        this.examEndAt = examEndAt;
        this.resultAt = resultAt;
    }
}
