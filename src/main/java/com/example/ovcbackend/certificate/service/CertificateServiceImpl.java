package com.example.ovcbackend.certificate.service;

import com.example.ovcbackend.certificate.dto.CertResponse;
import com.example.ovcbackend.certificate.entity.Certificate;
import com.example.ovcbackend.certificate.repository.CertificateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CertificateServiceImpl implements CertificateService{
    private final CertificateRepository certificateRepository;

    // 자격증 단건 조회
    @Override
    public CertResponse getCertificate(Long certId) {
        // db에서 자격증 id로 해당 id의 자격증 조회하기
        Certificate certificate = certificateRepository.findById(certId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 자격증입니다."));

        return CertResponse.from(certificate);
    }
}
