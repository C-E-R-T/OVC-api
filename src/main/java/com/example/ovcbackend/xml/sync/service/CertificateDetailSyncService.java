package com.example.ovcbackend.xml.sync.service;

import com.example.ovcbackend.certificate.entity.Certificate;
import com.example.ovcbackend.certificate.repository.CertificateRepository;
import com.example.ovcbackend.xml.external.dto.CertificateDetailApiResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateDetailSyncService {
    // items가 비어 내려오는 간헐 이슈를 흡수하기 위한 최대 재시도 횟수
    private static final int MAX_EMPTY_ITEMS_RETRY = 3;
    // 재시도 간격(ms)
    private static final long RETRY_DELAY_MILLIS = 300L;

    private final CertificateRepository certificateRepository;
    private final RestTemplate restTemplate;
    private final TransactionTemplate transactionTemplate;

    @Value("${spring.openapi.cert-detail.base-url}")
    private String certDetailBaseUrl;

    @Value("${spring.openapi.cert-detail.key}")
    private String certDetailKey;

    // 기존 전체 자격증 대상 상세정보 동기화(레거시 호환용)
    public void updateCertificateDetails(String baseUrl, String serviceKey) {
        List<Certificate> certs = certificateRepository.findAll();
        syncCertificateDetails(certs, baseUrl, serviceKey, new LinkedHashMap<>());
    }

    // 전달된 종목코드 목록만 대상으로 상세정보 동기화
    public List<String> updateCertificateDetailsByCertIds(List<String> certIds) {
        return updateCertificateDetailsByCertIdsWithReasons(certIds).getSyncedCertIds();
    }

    // 전달된 종목코드 목록만 대상으로 상세정보 동기화(스킵 사유 포함)
    public DetailSyncResult updateCertificateDetailsByCertIdsWithReasons(List<String> certIds) {
        if (certIds == null || certIds.isEmpty()) {
            return DetailSyncResult.builder()
                    .syncedCertIds(List.of())
                    .skippedReasons(Map.of())
                    .build();
        }

        List<Certificate> certificates = new ArrayList<>();
        // key: jmCd, value: 스킵/실패 사유 코드
        Map<String, String> skippedReasons = new LinkedHashMap<>();
        for (String certId : certIds) {
            if (certId == null || certId.isBlank()) {
                skippedReasons.put(String.valueOf(certId), "blank_cert_id");
                continue;
            }
            String trimmed = certId.trim();
            certificateRepository.findByCertId(trimmed)
                    .ifPresentOrElse(
                            certificates::add,
                            () -> skippedReasons.put(trimmed, "certificate_not_found")
                    );
        }

        return syncCertificateDetails(certificates, certDetailBaseUrl, certDetailKey, skippedReasons);
    }

    // 상세 OpenAPI 호출 -> 파싱 -> certificate 상세 필드 업데이트
    private DetailSyncResult syncCertificateDetails(
            List<Certificate> certs,
            String baseUrl,
            String serviceKey,
            Map<String, String> skippedReasons
    ) {
        List<String> syncedCertIds = new ArrayList<>();

        for (Certificate cert : certs) {
            if (cert.getCertId() == null || cert.getCertId().isBlank()) {
                continue;
            }
            String jmCd = cert.getCertId().trim();

            if (hasDetailedInfo(cert)) {
                skippedReasons.put(jmCd, "already_populated");
                log.info("detail skip jmCd={}: already populated", cert.getCertId());
                continue;
            }

            String apiUrl = baseUrl + "?serviceKey=" + serviceKey + "&jmCd=" + jmCd;

            try {
                // 외부 API가 간헐적으로 빈 items를 반환하므로 재시도 후 응답 사용
                CertificateDetailApiResponse response = fetchWithRetryOnEmptyItems(apiUrl, jmCd);

                if (response == null || response.getBody() == null) {
                    skippedReasons.put(jmCd, "empty_response_body");
                    log.warn("detail skip jmCd={}: empty response body", jmCd);
                    continue;
                }

                List<CertificateDetailApiResponse.CertDetailItemDto> items = response.getBody().getItems();
                if (items == null || items.isEmpty()) {
                    skippedReasons.put(jmCd, "empty_items_after_retries");
                    log.info("detail skip jmCd={}: items empty after retries", jmCd);
                    continue;
                }

                log.info("detail items count jmCd={}: {}", jmCd, items.size());

                String[] data = parseContents(items);
                if (isAllEmpty(data)) {
                    skippedReasons.put(jmCd, "parsed_all_fields_empty");
                    log.info("detail skip jmCd={}: parsed fields empty", jmCd);
                    continue;
                }

                saveCertificateDetails(cert, data);
                syncedCertIds.add(jmCd);
                skippedReasons.remove(jmCd);

                log.info("detail saved jmCd={}, dept={}, subject={}, trend={}, method={}, criteria={}",
                        jmCd,
                        summarize(data[0]),
                        summarize(data[1]),
                        summarize(data[2]),
                        summarize(data[3]),
                        summarize(data[4]));

            } catch (Exception e) {
                skippedReasons.put(jmCd, "error:" + e.getClass().getSimpleName());
                log.error("detail API error jmCd={}: {}", jmCd, e.getMessage(), e);
            }
        }

        return DetailSyncResult.builder()
                .syncedCertIds(List.copyOf(syncedCertIds))
                .skippedReasons(Map.copyOf(skippedReasons))
                .build();
    }

    // 일부 종목코드에서 간헐적으로 items가 비어 내려오는 케이스를 짧게 재시도
    private CertificateDetailApiResponse fetchWithRetryOnEmptyItems(String apiUrl, String jmCd) {
        CertificateDetailApiResponse lastResponse = null;

        for (int attempt = 1; attempt <= MAX_EMPTY_ITEMS_RETRY; attempt++) {
            lastResponse = restTemplate.getForObject(apiUrl, CertificateDetailApiResponse.class);

            if (lastResponse != null
                    && lastResponse.getBody() != null
                    && lastResponse.getBody().getItems() != null
                    && !lastResponse.getBody().getItems().isEmpty()) {
                if (attempt > 1) {
                    log.info("detail retry recovered jmCd={} attempt={}", jmCd, attempt);
                }
                return lastResponse;
            }

            if (attempt < MAX_EMPTY_ITEMS_RETRY) {
                // 빈 응답 직후 즉시 재호출 시 동일 결과가 잦아 짧은 간격으로 완충
                sleepRetryDelay();
            }
        }

        return lastResponse;
    }

    // 외부 API 연속 호출 완충
    private void sleepRetryDelay() {
        try {
            Thread.sleep(RETRY_DELAY_MILLIS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // 파싱된 상세 값들을 엔티티에 반영 후 저장
    private void saveCertificateDetails(Certificate cert, String[] data) {
        transactionTemplate.executeWithoutResult(status -> {
            cert.updateDetailedInfo(
                    emptyToNull(data[0]),
                    emptyToNull(data[1]),
                    emptyToNull(data[2]),
                    emptyToNull(data[3]),
                    emptyToNull(data[4])
            );
            certificateRepository.save(cert);
        });
    }

    // infogb/contents 조합에서 서비스에 필요한 5개 상세 속성 추출
    private String[] parseContents(List<CertificateDetailApiResponse.CertDetailItemDto> items) {
        String examTrend = null;
        String acqMethod = null;

        String dept = null;
        String subject = null;
        String criteria = null;

        for (CertificateDetailApiResponse.CertDetailItemDto item : items) {
            String infoGb = item.getInfogb() == null ? null : item.getInfogb().trim();
            String cleaned = cleanHtml(item.getContents());

            log.debug("detail item infoGb={}, content={}", infoGb, summarize(cleaned));

            if (infoGb == null || cleaned == null || cleaned.isBlank()) {
                continue;
            }

            if ("출제경향".equals(infoGb)) {
                examTrend = cleaned;
            }

            if ("취득방법".equals(infoGb)) {
                dept = extractByPattern(cleaned,
                        "관련\\s*학과\\s*[:：-]?\\s*(.*?)(?=시험\\s*과목|검정\\s*방법|합격\\s*기준|취득\\s*방법|$)");

                subject = extractByPattern(cleaned,
                        "시험\\s*과목\\s*[:：-]?\\s*(.*?)(?=검정\\s*방법|합격\\s*기준|취득\\s*방법|$)");

                acqMethod = extractByPattern(cleaned,
                        "검정\\s*방법\\s*[:：-]?\\s*(.*?)(?=합격\\s*기준|취득\\s*방법|$)");

                criteria = extractByPattern(cleaned,
                        "합격\\s*기준\\s*[:：-]?\\s*(.*?)(?=취득\\s*방법|$)");
            }
        }

        return new String[]{dept, subject, examTrend, acqMethod, criteria};
    }

    // 항목별 텍스트에서 특정 블록을 정규식으로 추출
    private String extractByPattern(String content, String regex) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                regex,
                java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL
        );
        java.util.regex.Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }

    // HTML/엔티티/불필요 스타일을 제거해 파싱 가능한 일반 텍스트로 정리
    private String cleanHtml(String content) {
        if (content == null) {
            return "";
        }

        String cleaned = decodeNumericEntities(content);
        cleaned = cleaned.replaceAll("(?s)BODY \\{.*?\\}", " ");
        cleaned = cleaned.replaceAll("(?s)P \\{.*?\\}", " ");
        cleaned = cleaned.replaceAll("(?s)LI \\{.*?\\}", " ");
        cleaned = cleaned.replaceAll("(?i)</?(p|li|br)\\s*/?>", " ");
        cleaned = cleaned.replace("&lt;", "<").replace("&gt;", ">");
        cleaned = cleaned.replaceAll("[①-⑳]", " ");
        cleaned = cleaned.replaceAll("<([^>]+)>", " $1 ");
        cleaned = cleaned.replaceAll("&middot;", "·");
        cleaned = cleaned.replace("&amp;", "&");

        return cleaned.replaceAll("\\s+", " ").trim();
    }

    // 일부 숫자 엔티티를 유니코드 문자로 치환
    private String decodeNumericEntities(String content) {
        Map<String, String> replacements = Map.of(
                "&#9312;", "①",
                "&#9313;", "②",
                "&#9314;", "③",
                "&#9315;", "④",
                "&#9316;", "⑤"
        );

        String decoded = content;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            decoded = decoded.replace(entry.getKey(), entry.getValue());
        }
        return decoded;
    }

    // 빈 문자열을 null로 표준화
    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    // 파싱 결과 5개 필드가 전부 비어있는지 확인
    private boolean isAllEmpty(String[] values) {
        if (values == null || values.length == 0) {
            return true;
        }
        for (String value : values) {
            if (hasText(value)) {
                return false;
            }
        }
        return true;
    }

    // 상세 정보가 이미 존재하면 재호출을 건너뛰기 위한 체크
    private boolean hasDetailedInfo(Certificate cert) {
        return hasText(cert.getRelatedDepartment())
                || hasText(cert.getExamSubject())
                || hasText(cert.getExamTrend())
                || hasText(cert.getAcqMethod())
                || hasText(cert.getPassCriteria());
    }

    // null/blank 여부 공통 체크
    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    // 로그 출력을 위한 길이 제한 문자열 요약
    private String summarize(String value) {
        if (value == null || value.isBlank()) {
            return "null";
        }
        return value.length() > 40 ? value.substring(0, 40) + "..." : value;
    }

    @Getter
    @Builder
    public static class DetailSyncResult {
        // 상세 동기화가 실제 저장까지 완료된 종목코드 목록
        private List<String> syncedCertIds;
        // 상세 동기화가 스킵/실패된 종목코드별 사유
        private Map<String, String> skippedReasons;
    }
}
