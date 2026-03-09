package com.example.ovcbackend.certificate.service;

import com.example.ovcbackend.certificate.dto.CertResponse;

public interface CertificateService {
    CertResponse getCertificate(Long certId);
}
