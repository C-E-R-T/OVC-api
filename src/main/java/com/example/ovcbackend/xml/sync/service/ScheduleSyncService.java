package com.example.ovcbackend.xml.sync.service;

import com.example.ovcbackend.certificate.entity.Certificate;
import com.example.ovcbackend.certificate.repository.CertificateRepository;
import com.example.ovcbackend.schedule.ExamType;
import com.example.ovcbackend.schedule.entity.Schedule;
import com.example.ovcbackend.schedule.repository.ScheduleRepository;
import com.example.ovcbackend.xml.external.dto.ScheduleApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleSyncService {

    private static final String QUALIFICATION_TYPE_TECHNICAL = "T";
    private static final DateTimeFormatter BASIC_DATE = DateTimeFormatter.BASIC_ISO_DATE;
    private static final String NORMAL_RESULT_CODE = "00";
    private static final int REQUEST_ROWS = 50;

    private final CertificateRepository certificateRepository;
    private final ScheduleRepository scheduleRepository;
    private final RestTemplate restTemplate;
    private final TransactionTemplate transactionTemplate;

    @Value("${spring.openapi.schedule.base-url}")
    private String scheduleBaseUrl;

    @Value("${spring.openapi.schedule.key}")
    private String scheduleKey;

    public List<String> syncCurrentYearSchedulesForAllCertificates() {
        List<String> certIds = certificateRepository.findAll().stream()
                .map(Certificate::getCertId)
                .filter(this::hasText)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());

        return syncCurrentYearSchedules(certIds);
    }

    public List<String> syncCurrentYearSchedules(List<String> requestedCertIds) {
        int currentYear = Year.now().getValue();
        List<Certificate> certificates = resolveCertificates(requestedCertIds);
        List<String> syncedCertIds = new ArrayList<>();

        for (Certificate certificate : certificates) {
            if (certificate.getCertId() == null || certificate.getCertId().isBlank()) {
                continue;
            }

            URI apiUri = UriComponentsBuilder.fromHttpUrl(scheduleBaseUrl)
                    .queryParam("serviceKey", scheduleKey)
                    .queryParam("dataFormat", "xml")
                    .queryParam("numOfRows", REQUEST_ROWS)
                    .queryParam("pageNo", 1)
                    .queryParam("implYy", currentYear)
                    .queryParam("qualgbCd", QUALIFICATION_TYPE_TECHNICAL)
                    .queryParam("jmCd", certificate.getCertId())
                    .build(true)
                    .toUri();

            try {
                ScheduleApiResponse response = restTemplate.getForObject(apiUri, ScheduleApiResponse.class);

                if (!isSuccessfulResponse(response)) {
                    log.warn("schedule API abnormal response certId={}, resultCode={}, resultMsg={}",
                            certificate.getCertId(),
                            extractResultCode(response),
                            extractResultMessage(response));
                    continue;
                }

                if (response.getBody() == null || response.getBody().getItems() == null) {
                    log.info("schedule skip certId={}: items empty", certificate.getCertId());
                    continue;
                }

                log.info("schedule items count certId={}: itemCount={}, totalCount={}",
                        certificate.getCertId(),
                        response.getBody().getItems().size(),
                        response.getBody().getTotalCount());

                for (ScheduleApiResponse.ScheduleItemDto item : response.getBody().getItems()) {
                    upsertWrittenSchedule(certificate, item);
                    upsertPracticalSchedule(certificate, item);
                }
                syncedCertIds.add(certificate.getCertId());
            } catch (Exception e) {
                log.error("schedule API error certId={}: {}", certificate.getCertId(), e.getMessage(), e);
            }
        }

        return syncedCertIds;
    }

    private void upsertWrittenSchedule(Certificate certificate, ScheduleApiResponse.ScheduleItemDto item) {
        if (!hasRequiredDates(item.getDocRegStartDt(), item.getDocRegEndDt(), item.getDocExamStartDt(), item.getDocExamEndDt(), item.getDocPassDt())) {
            log.info("schedule skip examType=WRITTEN certId={}, round={}-{}, reason=missing dates, regStart={}, regEnd={}, examStart={}, examEnd={}, result={}",
                    certificate.getCertId(),
                    item.getImplYy(),
                    item.getImplSeq(),
                    summarize(item.getDocRegStartDt()),
                    summarize(item.getDocRegEndDt()),
                    summarize(item.getDocExamStartDt()),
                    summarize(item.getDocExamEndDt()),
                    summarize(item.getDocPassDt()));
            return;
        }

        saveSchedule(
                certificate,
                item,
                ExamType.WRITTEN,
                item.getDocRegStartDt(),
                item.getDocRegEndDt(),
                item.getDocExamStartDt(),
                item.getDocExamEndDt(),
                item.getDocPassDt()
        );
    }

    private void upsertPracticalSchedule(Certificate certificate, ScheduleApiResponse.ScheduleItemDto item) {
        if (!hasRequiredDates(item.getPracRegStartDt(), item.getPracRegEndDt(), item.getPracExamStartDt(), item.getPracExamEndDt(), item.getPracPassDt())) {
            log.info("schedule skip examType=PRACTICAL certId={}, round={}-{}, reason=missing dates, regStart={}, regEnd={}, examStart={}, examEnd={}, result={}",
                    certificate.getCertId(),
                    item.getImplYy(),
                    item.getImplSeq(),
                    summarize(item.getPracRegStartDt()),
                    summarize(item.getPracRegEndDt()),
                    summarize(item.getPracExamStartDt()),
                    summarize(item.getPracExamEndDt()),
                    summarize(item.getPracPassDt()));
            return;
        }

        saveSchedule(
                certificate,
                item,
                ExamType.PRACTICAL,
                item.getPracRegStartDt(),
                item.getPracRegEndDt(),
                item.getPracExamStartDt(),
                item.getPracExamEndDt(),
                item.getPracPassDt()
        );
    }

    private void saveSchedule(
            Certificate certificate,
            ScheduleApiResponse.ScheduleItemDto item,
            ExamType examType,
            String applyStart,
            String applyEnd,
            String examStart,
            String examEnd,
            String result
    ) {
        String examRound = item.getImplYy() + "-" + item.getImplSeq();

        transactionTemplate.executeWithoutResult(status -> {
            Schedule schedule = scheduleRepository
                    .findByCertificateIdAndExamTypeAndExamRound(certificate.getId(), examType, examRound)
                    .orElseGet(() -> Schedule.builder()
                            .certificate(certificate)
                            .examType(examType)
                            .examRound(examRound)
                            .examName(item.getDescription())
                            .applyStartAt(parseStartOfDay(applyStart))
                            .applyEndAt(parseEndOfDay(applyEnd))
                            .examStartAt(parseStartOfDay(examStart))
                            .examEndAt(parseEndOfDay(examEnd))
                            .resultAt(parseStartOfDay(result))
                            .build());

            schedule.updateSchedule(
                    item.getDescription(),
                    examRound,
                    examType,
                    parseStartOfDay(applyStart),
                    parseEndOfDay(applyEnd),
                    parseStartOfDay(examStart),
                    parseEndOfDay(examEnd),
                    parseStartOfDay(result)
            );

            scheduleRepository.save(schedule);

            log.info("schedule saved certId={}, examType={}, round={}, examName={}",
                    certificate.getCertId(),
                    examType,
                    examRound,
                    summarize(item.getDescription()));
        });
    }

    private boolean hasRequiredDates(String applyStart, String applyEnd, String examStart, String examEnd, String result) {
        return hasText(applyStart) && hasText(applyEnd) && hasText(examStart) && hasText(examEnd) && hasText(result);
    }

    private boolean isSuccessfulResponse(ScheduleApiResponse response) {
        return response != null
                && response.getHeader() != null
                && NORMAL_RESULT_CODE.equals(response.getHeader().getResultCode());
    }

    private String extractResultCode(ScheduleApiResponse response) {
        if (response == null || response.getHeader() == null) {
            return "null";
        }
        return summarize(response.getHeader().getResultCode());
    }

    private String extractResultMessage(ScheduleApiResponse response) {
        if (response == null || response.getHeader() == null) {
            return "null";
        }
        return summarize(response.getHeader().getResultMsg());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private LocalDateTime parseStartOfDay(String value) {
        LocalDate date = LocalDate.parse(value, BASIC_DATE);
        return date.atStartOfDay();
    }

    private LocalDateTime parseEndOfDay(String value) {
        LocalDate date = LocalDate.parse(value, BASIC_DATE);
        return date.atTime(23, 59, 59);
    }

    private String summarize(String value) {
        if (value == null || value.isBlank()) {
            return "null";
        }
        return value.length() > 40 ? value.substring(0, 40) + "..." : value;
    }

    private List<Certificate> resolveCertificates(List<String> requestedCertIds) {
        if (requestedCertIds == null || requestedCertIds.isEmpty()) {
            return List.of();
        }

        List<Certificate> certificates = new ArrayList<>();
        for (String requestedCertId : requestedCertIds) {
            if (requestedCertId == null || requestedCertId.isBlank()) {
                continue;
            }

            certificateRepository.findByCertId(requestedCertId.trim())
                    .ifPresentOrElse(certificates::add, () ->
                            log.info("schedule skip certId={}: certificate not found", requestedCertId));
        }
        return certificates;
    }
}
