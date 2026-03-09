package com.example.ovcbackend.certificate.controller;

import com.example.ovcbackend.certificate.dto.CertResponse;
import com.example.ovcbackend.certificate.service.CertificateService;
import com.example.ovcbackend.global.commonResponse.OkResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/certs")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    @GetMapping("/{certId}")
    public ResponseEntity<OkResponse<CertResponse>> getCertificate(@PathVariable Long certId
            , HttpServletRequest request) {
        CertResponse certResponse = certificateService.getCertificate(certId);
        return ResponseEntity.ok(OkResponse.success(certResponse,request.getRequestURI()));
    }

}
