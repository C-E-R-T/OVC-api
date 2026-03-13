package com.example.ovcbackend.xml.sync.service;

import com.example.ovcbackend.category.repository.CategoryRepository;
import com.example.ovcbackend.certificate.repository.CertificateRepository;
import com.example.ovcbackend.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.openapi.sync.bootstrap", name = "enabled", havingValue = "true")
public class OpenApiSyncRunner implements ApplicationRunner {

    private final PopularCertificateSyncService popularCertificateSyncService;
    private final CategoryRepository categoryRepository;
    private final CertificateRepository certificateRepository;
    private final ScheduleRepository scheduleRepository;

    // 애플리케이션 시작 시 1회 부트스트랩 동기화 실행
    @Override
    public void run(ApplicationArguments args) {
        if (!isBootstrapRequired()) {
            log.info("bootstrap sync skipped: database already contains seed data");
            return;
        }

        PopularCertificateSyncService.SyncSummary summary =
                popularCertificateSyncService.syncConfiguredPopularCertificates();
        log.info("bootstrap sync completed: matchedCount={}, detailCount={}, scheduleCount={}",
                summary.getMatchedNames().size(),
                summary.getDetailSyncedCertIds().size(),
                summary.getScheduleSyncedCertIds().size());
    }

    // DB가 완전히 비어있는 초기 상태인지 판단
    private boolean isBootstrapRequired() {
        return categoryRepository.count() == 0
                && certificateRepository.count() == 0
                && scheduleRepository.count() == 0;
    }
}
