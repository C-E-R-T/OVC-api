package com.example.ovcbackend.xml.sync.controller;

import com.example.ovcbackend.global.commonResponse.OkResponse;
import com.example.ovcbackend.xml.sync.dto.PopularCertificateSyncRequest;
import com.example.ovcbackend.xml.sync.dto.PopularCertificateSyncResult;
import com.example.ovcbackend.xml.sync.service.PopularCertificateSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "popular certificate sync Controller", description = "인기 자격증 선별 동기화 api")
@RestController
@RequestMapping("/api/admin/sync")
@RequiredArgsConstructor
public class PopularCertificateSyncController {

    private final PopularCertificateSyncService popularCertificateSyncService;

    // 전달받은 인기 자격증 이름 목록 기준으로 기본/상세/일정 동기화 실행
    @Operation(
            summary = "자격증 정보 동기화",
            description = "전달받은 자격증 이름 목록을 기준으로 기본 정보, 상세 정보, 시험 일정을 API와 동기화합니다."
    )
    @PostMapping("/popular-certificates")
    public ResponseEntity<OkResponse<PopularCertificateSyncResult>> syncPopularCertificates(
            @Valid @RequestBody PopularCertificateSyncRequest requestBody,
            HttpServletRequest request
    ) {
        PopularCertificateSyncResult result =
                popularCertificateSyncService.syncPopularCertificates(requestBody.getCertificateNames());

        return ResponseEntity.ok(
                OkResponse.success("인기 자격증 동기화가 완료되었습니다.", result, request.getRequestURI())
        );
    }
}
