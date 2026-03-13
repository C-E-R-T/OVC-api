package com.example.ovcbackend.xml.sync.service;

import com.example.ovcbackend.xml.sync.dto.PopularCertificateSyncResult;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

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

        List<String> detailSyncedCertIds =
                certificateDetailSyncService.updateCertificateDetailsByCertIds(categorySyncResult.getCertIds());
        List<String> scheduleSyncedCertIds = scheduleSyncService.syncCurrentYearSchedules(categorySyncResult.getCertIds());

        return SyncSummary.builder()
                .requestedNames(categorySyncResult.getRequestedNames())
                .matchedNames(categorySyncResult.getMatchedNames())
                .missingNames(categorySyncResult.getMissingNames())
                .categoryNames(categorySyncResult.getCategoryNames())
                .certIds(categorySyncResult.getCertIds())
                .detailSyncedCertIds(detailSyncedCertIds)
                .scheduleSyncedCertIds(scheduleSyncedCertIds)
                .build();
    }

    @Getter
    @Builder
    public static class SyncSummary {
        private List<String> requestedNames;
        private List<String> matchedNames;
        private List<String> missingNames;
        private List<String> categoryNames;
        private List<String> certIds;
        private List<String> detailSyncedCertIds;
        private List<String> scheduleSyncedCertIds;
    }
}
