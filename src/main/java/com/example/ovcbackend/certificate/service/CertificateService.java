package com.example.ovcbackend.certificate.service;

import com.example.ovcbackend.certificate.dto.CertResponse;
import com.example.ovcbackend.certificate.dto.CertSearchResponse;
import com.example.ovcbackend.certificate.dto.CertificateRankResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CertificateService {
    CertResponse getCertificate(Long certId);

    Page<CertSearchResponse> searchCertificates(List<Long> categoryId, String keyword, Pageable pageable);

    List<CertificateRankResponse> getTop3PopularCertificates();
}
