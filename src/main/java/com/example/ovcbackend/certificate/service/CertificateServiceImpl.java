package com.example.ovcbackend.certificate.service;

import com.example.ovcbackend.certificate.dto.CertResponse;

import com.example.ovcbackend.certificate.dto.CertSearchResponse;
import com.example.ovcbackend.certificate.dto.CertificateRankResponse;
import com.example.ovcbackend.certificate.entity.Certificate;
import com.example.ovcbackend.certificate.exception.CertNotFoundException;
import com.example.ovcbackend.certificate.repository.CertificateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CertificateServiceImpl implements CertificateService{
    private final CertificateRepository certificateRepository;

    // 자격증 단건 조회
    @Override
    public CertResponse getCertificate(Long certId) {
        // db에서 자격증 id로 해당 id의 자격증 조회하기
        log.info("[/certs/{certId}] 자격증 단건 조회 시작 -certID: {}", certId);
        Certificate certificate = certificateRepository.findById(certId)
                .orElseThrow(() ->{
                        log.error("[/certs/{certId}] 자격증 조회 실패 - ID: {}를 찾을 수 없음", certId);
                        return new CertNotFoundException("존재하지 않는 자격증입니다.");
                });


        return CertResponse.from(certificate);
    }

    @Override
    public Page<CertSearchResponse> searchCertificates(List<Long> categoryIds, String keyword, Pageable pageable){
       Page<Certificate> certificatePage;
        if(categoryIds == null || categoryIds.isEmpty()){
            certificatePage = certificateRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else {
            certificatePage = certificateRepository.findByCategoryIdInAndNameContainingIgnoreCase(
                    categoryIds, keyword, pageable
            );
        }
        return certificatePage.map(cert -> CertSearchResponse.builder()
                .certId(cert.getId())
                .name(cert.getName())
                        .authority(cert.getAuthority())
                        .description(cert.getDescription())
                        .categoryId(cert.getCategoryId())
                        .build()
                );

    }

    @Override
    public List<CertificateRankResponse> getTop3PopularCertificates() {
        return certificateRepository.findTop3CertificateWithCount(PageRequest.of(0, 3));
    }

}
