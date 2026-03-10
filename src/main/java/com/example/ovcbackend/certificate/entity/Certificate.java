package com.example.ovcbackend.certificate.entity;


import com.example.ovcbackend.category.entity.Category;
import com.example.ovcbackend.global.entity.BaseTime;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;



@Entity
@Table(name = "certificates")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Certificate extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String authority;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false,insertable = false, updatable = false)
    private Category category;

    @Column(name = "category_id")
    private Long categoryId;
}
