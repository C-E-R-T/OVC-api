package com.example.ovcbackend.schedule.entity;

import com.example.ovcbackend.certificate.entity.Certificate;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "schedules")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cert_id", nullable = false)
    private Certificate certificate;

    @Column(name = "exam_name", length = 100, nullable = false)
    private String examName;

    @Column(name = "exam_type", length = 20)
    private String examType = "WRITTEN";

    @Column(name="apply_start_at", nullable = false)
    private LocalDateTime applyStartAt;

    @Column(name = "apply_end_at", nullable = false)
    private LocalDateTime applyEndAt;

    @Column(name="exam_at", nullable = false)
    private LocalDateTime examAt;

    @Column(name = "result_at", nullable = false)
    private LocalDateTime result_at;

    @CreatedDate
    @Column(name="created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
