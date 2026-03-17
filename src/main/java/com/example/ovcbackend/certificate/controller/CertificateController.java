package com.example.ovcbackend.certificate.controller;

import com.example.ovcbackend.certificate.dto.CertResponse;
import com.example.ovcbackend.certificate.dto.CertSearchResponse;
import com.example.ovcbackend.certificate.dto.CertificateRankResponse;
import com.example.ovcbackend.certificate.service.CertificateService;
import com.example.ovcbackend.global.commonResponse.OkResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Certificate controller", description = "자격증 관련 api")
@RestController
@RequestMapping("/api/certs")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;

    @Operation(summary = "자격증 상세 조회", description = "특정 ID를 가진 자격증의 상세 정보를 조회합니다.")
    @GetMapping("/{certId}")
    public ResponseEntity<OkResponse<CertResponse>> getCertificate(
            @Parameter(description = "자격증 id") @PathVariable Long certId
            , HttpServletRequest request) {
        CertResponse certResponse = certificateService.getCertificate(certId);
        return ResponseEntity.ok(OkResponse.success(certResponse,request.getRequestURI()));
    }

    @Operation(
            summary = "자격증 검색 및 필터링 기능",
            description = "카테고리 id 목록이나 키워드를 통해 자격증을 검색합니다."
    )
    @GetMapping
    public ResponseEntity<OkResponse<Map<String, Object>>> searchCertificates(
            @Parameter(description = "필터링할 카테고리")
            @RequestParam(name = "categoryIds", required = false) List<Long> categoryIds,
            @Parameter(description = "검색 키워드(자격증 이름 등)")
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @org.springdoc.core.annotations.ParameterObject // 스웨거로 page를 파라미터로 받으려면 이와 같이 설정해줘야 됨.
            @PageableDefault(page = 0, size = 6, sort="name", direction = Sort.Direction.ASC) Pageable pageable,
            HttpServletRequest request
    ) {
        Page<CertSearchResponse> response = certificateService.searchCertificates(categoryIds,keyword,pageable);

        return ResponseEntity.ok(OkResponse.successPage(response, request.getRequestURI()));
    }

    @Operation(summary = "인기 자격증 TOP 3 조회", description = "가장 인기 있는 자격증 3개를 조회합니다.")
    @GetMapping("/rank")
    public ResponseEntity<OkResponse<List<CertificateRankResponse>>> getPopularCertificates(
            HttpServletRequest request
    ) {
        List<CertificateRankResponse> responses = certificateService.getTop3PopularCertificates();
        return ResponseEntity.ok(OkResponse.success(responses, request.getRequestURI()));
    }



}
