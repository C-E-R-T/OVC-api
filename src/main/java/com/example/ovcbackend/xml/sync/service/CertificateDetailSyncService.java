package com.example.ovcbackend.xml.sync.service;

import com.example.ovcbackend.certificate.entity.Certificate;
import com.example.ovcbackend.certificate.repository.CertificateRepository;
import com.example.ovcbackend.xml.external.dto.CertificateDetailApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateDetailSyncService {

    private final CertificateRepository certificateRepository;
    private final RestTemplate restTemplate;
    private final TransactionTemplate transactionTemplate;

    @Value("${spring.openapi.cert-detail.base-url}")
    private String certDetailBaseUrl;

    @Value("${spring.openapi.cert-detail.key}")
    private String certDetailKey;

    public void updateCertificateDetails(String baseUrl, String serviceKey) {
        List<Certificate> certs = certificateRepository.findAll();
        syncCertificateDetails(certs, baseUrl, serviceKey);
    }

    public List<String> updateCertificateDetailsByCertIds(List<String> certIds) {
        if (certIds == null || certIds.isEmpty()) {
            return List.of();
        }

        List<Certificate> certificates = new ArrayList<>();
        for (String certId : certIds) {
            if (certId == null || certId.isBlank()) {
                continue;
            }
            certificateRepository.findByCertId(certId.trim()).ifPresent(certificates::add);
        }

        return syncCertificateDetails(certificates, certDetailBaseUrl, certDetailKey);
    }

    private List<String> syncCertificateDetails(List<Certificate> certs, String baseUrl, String serviceKey) {
        List<String> syncedCertIds = new ArrayList<>();

        for (Certificate cert : certs) {
            if (cert.getCertId() == null || cert.getCertId().isBlank()) {
                continue;
            }

            if (hasDetailedInfo(cert)) {
                log.info("detail skip jmCd={}: already populated", cert.getCertId());
                continue;
            }

            String jmCd = cert.getCertId().trim();
            String apiUrl = baseUrl + "?serviceKey=" + serviceKey + "&jmCd=" + jmCd;

            try {
                CertificateDetailApiResponse response =
                        restTemplate.getForObject(apiUrl, CertificateDetailApiResponse.class);

                if (response == null || response.getBody() == null) {
                    log.warn("detail skip jmCd={}: empty response body", jmCd);
                    continue;
                }

                List<CertificateDetailApiResponse.CertDetailItemDto> items = response.getBody().getItems();
                if (items == null || items.isEmpty()) {
                    log.info("detail skip jmCd={}: items empty", jmCd);
                    continue;
                }

                log.info("detail items count jmCd={}: {}", jmCd, items.size());

                String[] data = parseContents(items);

                saveCertificateDetails(cert, data);
                syncedCertIds.add(jmCd);

                log.info("detail saved jmCd={}, dept={}, subject={}, trend={}, method={}, criteria={}",
                        jmCd,
                        summarize(data[0]),
                        summarize(data[1]),
                        summarize(data[2]),
                        summarize(data[3]),
                        summarize(data[4]));

            } catch (Exception e) {
                log.error("detail API error jmCd={}: {}", jmCd, e.getMessage(), e);
            }
        }

        return syncedCertIds;
    }

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

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private boolean hasDetailedInfo(Certificate cert) {
        return hasText(cert.getRelatedDepartment())
                || hasText(cert.getExamSubject())
                || hasText(cert.getExamTrend())
                || hasText(cert.getAcqMethod())
                || hasText(cert.getPassCriteria());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String summarize(String value) {
        if (value == null || value.isBlank()) {
            return "null";
        }
        return value.length() > 40 ? value.substring(0, 40) + "..." : value;
    }
}
