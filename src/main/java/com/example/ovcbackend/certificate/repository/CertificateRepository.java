package com.example.ovcbackend.certificate.repository;

import com.example.ovcbackend.certificate.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    // 카테고리 ID로 자격증 목록 조회
    List<Certificate> findByCategoryId(Long categoryId);
}
