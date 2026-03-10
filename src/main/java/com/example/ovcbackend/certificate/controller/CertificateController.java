package com.example.ovcbackend.certificate.controller;

import com.example.ovcbackend.certificate.dto.CertResponse;
import com.example.ovcbackend.certificate.dto.CertSearchResponse;
import com.example.ovcbackend.certificate.service.CertificateService;
import com.example.ovcbackend.global.commonResponse.OkResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    @GetMapping
    public ResponseEntity<OkResponse<Map<String, Object>>> searchCertificates(
            @RequestParam(name = "categoryIds", required = false) List<Long> categoryIds,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @PageableDefault(page = 0, size = 6, sort="name", direction = Sort.Direction.ASC) Pageable pageable,
            HttpServletRequest request
    ) {
        Page<CertSearchResponse> response = certificateService.searchCertificates(categoryIds,keyword,pageable);

        return ResponseEntity.ok(OkResponse.successPage(response, request.getRequestURI()));
    }



}
