package com.example.ovcbackend.xml.sync.service;

import com.example.ovcbackend.xml.sync.dto.PopularCertificateSyncResult;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PopularCertificateSyncService {

    private final CertificateCategorySyncService certificateCategorySyncService;
    private final CertificateDetailSyncService certificateDetailSyncService;
    private final ScheduleSyncService scheduleSyncService;

    @Value("${spring.openapi.sync.popular-certificate-names}")
    private String popularCertificateNames;

    // 요청 이름 목록을 기준으로 기본/상세/일정 동기화를 순차 실행
    public PopularCertificateSyncResult syncPopularCertificates(List<String> certificateNames) {
        SyncSummary summary = syncCertificates(certificateNames);

        return PopularCertificateSyncResult.builder()
                .requestedNames(summary.getRequestedNames())
                .matchedNames(summary.getMatchedNames())
                .missingNames(summary.getMissingNames())
                .categoryNames(summary.getCategoryNames())
                .certIds(summary.getCertIds())
                .detailSyncedCertIds(summary.getDetailSyncedCertIds())
                .detailSkippedReasons(summary.getDetailSkippedReasons())
                .scheduleSyncedCertIds(summary.getScheduleSyncedCertIds())
                .build();
    }

    // 설정값(popular-certificate-names) 기반으로 전체 동기화 실행
    public SyncSummary syncConfiguredPopularCertificates() {
        List<String> certificateNames = Arrays.stream(popularCertificateNames.split(","))
                .map(String::trim)
                .filter(name -> !name.isBlank())
                .toList();

        return syncCertificates(certificateNames);
    }

    // 공통 동기화 파이프라인: category/certificate -> detail -> schedule
    private SyncSummary syncCertificates(List<String> certificateNames) {
        CertificateCategorySyncService.TargetCertificateSyncResult categorySyncResult =
                certificateCategorySyncService.syncCertificatesByNames(certificateNames);

        CertificateDetailSyncService.DetailSyncResult detailSyncResult =
                certificateDetailSyncService.updateCertificateDetailsByCertIdsWithReasons(categorySyncResult.getCertIds());
        // quota 절약을 위해 상세 동기화 이후 일정 동기화를 같은 매칭 집합으로 이어서 실행
        List<String> scheduleSyncedCertIds = scheduleSyncService.syncCurrentYearSchedules(categorySyncResult.getCertIds());

        return SyncSummary.builder()
                .requestedNames(categorySyncResult.getRequestedNames())
                .matchedNames(categorySyncResult.getMatchedNames())
                .missingNames(categorySyncResult.getMissingNames())
                .categoryNames(categorySyncResult.getCategoryNames())
                .certIds(categorySyncResult.getCertIds())
                .detailSyncedCertIds(detailSyncResult.getSyncedCertIds())
                .detailSkippedReasons(detailSyncResult.getSkippedReasons())
                .scheduleSyncedCertIds(scheduleSyncedCertIds)
                .build();
    }

    @Getter
    @Builder
    public static class SyncSummary {
        // 요청으로 들어온 자격증명
        private List<String> requestedNames;
        // 매칭 성공 자격증명
        private List<String> matchedNames;
        // 매칭 실패 자격증명
        private List<String> missingNames;
        // 매칭된 카테고리명
        private List<String> categoryNames;
        // 매칭된 종목코드 목록
        private List<String> certIds;
        // 상세 동기화 성공 종목코드
        private List<String> detailSyncedCertIds;
        // 상세 동기화 스킵/실패 사유
        private Map<String, String> detailSkippedReasons;
        // 일정 동기화 성공 종목코드
        private List<String> scheduleSyncedCertIds;
    }
}
