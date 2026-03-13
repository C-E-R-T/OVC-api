package com.example.ovcbackend.certificate.entity;


import com.example.ovcbackend.category.entity.Category;
import com.example.ovcbackend.global.entity.BaseTime;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;



@Entity
@Table(name = "certificates")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Certificate extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String authority;

    @Column(name = "cert_id", length = 50)
    private String certId;

    @Column(name = "exam_trend", columnDefinition = "TEXT")
    private String examTrend;

    @Column(name = "acq_method", columnDefinition = "TEXT")
    private String acqMethod;

    @Column(name = "precautions", columnDefinition = "TEXT")
    private String precautions;

    @Column(name="description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "written_fee")
    private Integer writtenFee;

    @Column(name = "practical_fee")
    private Integer practicalFee;

    @Column(name = "related_department", columnDefinition = "TEXT")
    private String relatedDepartment; // 관련학과

    @Column(name = "exam_subject" , columnDefinition = "TEXT")
    private String examSubject;       // 시험과목

    @Column(name = "pass_criteria" , columnDefinition = "TEXT")
    private String passCriteria;    // 합격기준

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false,insertable = false, updatable = false)
    private Category category;

    @Column(name = "category_id")
    private Long categoryId;

    @Builder
    public Certificate(String name, String authority, String certId, String examTrend,
                       String acqMethod, String precautions, String description,
                       Integer writtenFee, Integer practicalFee, String relatedDepartment,
                       String examSubject, String passCriteria, Long categoryId) {
        this.name = name;
        this.authority = authority;
        this.certId = certId;
        this.examTrend = examTrend;
        this.acqMethod = acqMethod;
        this.precautions = precautions;
        this.description = description;
        this.writtenFee = writtenFee;
        this.practicalFee = practicalFee;
        this.relatedDepartment = relatedDepartment;
        this.examSubject = examSubject;
        this.passCriteria = passCriteria;
        this.categoryId = categoryId;
    }


    // 카테고리/기관/종목코드 등 기본 메타 정보를 갱신
    public void updateBasicInfo(String authority, String certId, Long categoryId) {
        this.authority = authority;
        this.certId = certId;
        this.categoryId = categoryId;
        this.description = null;
    }

    // 상세 API에서 내려온 부가 정보를 갱신
    public void updateDetailedInfo(String dept, String subject, String trend, String method, String passCriteria) {
        this.relatedDepartment = dept;
        this.examSubject = subject;
        this.examTrend = trend;
        this.acqMethod = method;
        this.passCriteria = passCriteria;
    }
}
