package com.example.ovcbackend.xml.sync.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.openapi.sync.monthly", name = "enabled", havingValue = "true")
public class OpenApiSyncScheduler {

    private final PopularCertificateSyncService popularCertificateSyncService;

    // 매월 설정된 시각에 인기 자격증 동기화 파이프라인 실행
    @Scheduled(
            cron = "${spring.openapi.sync.monthly.cron}",
            zone = "${spring.openapi.sync.monthly.zone}"
    )
    public void syncConfiguredPopularCertificates() {
        PopularCertificateSyncService.SyncSummary summary =
                popularCertificateSyncService.syncConfiguredPopularCertificates();
        log.info("monthly sync completed: matchedCount={}, detailCount={}, scheduleCount={}",
                summary.getMatchedNames().size(),
                summary.getDetailSyncedCertIds().size(),
                summary.getScheduleSyncedCertIds().size());
    }
}
