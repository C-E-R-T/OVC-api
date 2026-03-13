package com.example.ovcbackend.certificate.repository;


import com.example.ovcbackend.certificate.entity.Certificate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    // 카테고리 ID로 자격증 목록 조회
    List<Certificate> findByCategoryId(Long categoryId);
    // 카테고리 다중 필터가 없을 때 검색 기능
    //jpa에서 ContainingIgnoreCase 등으로 페이지 네이션이 구현이 가능!
    Page<Certificate> findByCategoryIdInAndNameContainingIgnoreCase(List<Long> categoryIds, String keyword, Pageable pageable);

    // 카테고리 다중 필터가 없을 때 검색
    Page<Certificate> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    //xml에서 자격증이 이미 등록되어 있는지 이름으로 확인
    boolean existsByName(String name);

    Optional<Certificate> findByName(String name);

    // 외부 종목코드(cert_id)로 자격증 단건 조회
    Optional<Certificate> findByCertId(String certId);

}
