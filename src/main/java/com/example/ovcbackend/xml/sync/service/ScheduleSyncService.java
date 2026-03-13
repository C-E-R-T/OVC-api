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

    // DB에 존재하는 모든 자격증의 올해 일정을 동기화
    public List<String> syncCurrentYearSchedulesForAllCertificates() {
        List<String> certIds = certificateRepository.findAll().stream()
                .map(Certificate::getCertId)
                .filter(this::hasText)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());

        return syncCurrentYearSchedules(certIds);
    }

    // 전달된 종목코드 목록의 올해 일정만 조회하여 upsert
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

    // 필기 일정 필수 날짜가 모두 있을 때만 저장
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

    // 실기 일정 필수 날짜가 모두 있을 때만 저장
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

    // certificate + examType + examRound 기준으로 일정 upsert
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

    // 일정 저장에 필요한 날짜 필드 존재 여부 체크
    private boolean hasRequiredDates(String applyStart, String applyEnd, String examStart, String examEnd, String result) {
        return hasText(applyStart) && hasText(applyEnd) && hasText(examStart) && hasText(examEnd) && hasText(result);
    }

    // OpenAPI resultCode가 정상(00)인지 확인
    private boolean isSuccessfulResponse(ScheduleApiResponse response) {
        return response != null
                && response.getHeader() != null
                && NORMAL_RESULT_CODE.equals(response.getHeader().getResultCode());
    }

    // 에러 로그 출력용 resultCode 추출
    private String extractResultCode(ScheduleApiResponse response) {
        if (response == null || response.getHeader() == null) {
            return "null";
        }
        return summarize(response.getHeader().getResultCode());
    }

    // 에러 로그 출력용 resultMsg 추출
    private String extractResultMessage(ScheduleApiResponse response) {
        if (response == null || response.getHeader() == null) {
            return "null";
        }
        return summarize(response.getHeader().getResultMsg());
    }

    // null/blank 여부 공통 체크
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    // yyyyMMdd를 당일 00:00:00으로 변환
    private LocalDateTime parseStartOfDay(String value) {
        LocalDate date = LocalDate.parse(value, BASIC_DATE);
        return date.atStartOfDay();
    }

    // yyyyMMdd를 당일 23:59:59으로 변환
    private LocalDateTime parseEndOfDay(String value) {
        LocalDate date = LocalDate.parse(value, BASIC_DATE);
        return date.atTime(23, 59, 59);
    }

    // 로그 출력을 위한 길이 제한 문자열 요약
    private String summarize(String value) {
        if (value == null || value.isBlank()) {
            return "null";
        }
        return value.length() > 40 ? value.substring(0, 40) + "..." : value;
    }

    // 요청 종목코드 목록을 실제 certificate 엔티티 목록으로 해석
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
