package com.example.ovcbackend.xml.sync.service;

import com.example.ovcbackend.category.entity.Category;
import com.example.ovcbackend.category.repository.CategoryRepository;
import com.example.ovcbackend.certificate.entity.Certificate;
import com.example.ovcbackend.certificate.repository.CertificateRepository;
import com.example.ovcbackend.xml.external.dto.CertificateCategoryApiResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CertificateCategorySyncService {

    private static final int REQUEST_ROWS = 1000;
    private static final String QNET_AUTHORITY = "한국산업인력공단";

    private final CertificateRepository certificateRepository;
    private final CategoryRepository categoryRepository;
    private final RestTemplate restTemplate;

    @Value("${spring.openapi.cert-category.base-url}") //env값 받아오기
    private String certCategoryBaseUrl;

    @Value("${spring.openapi.cert-category.key}")
    private String certCategoryKey;

    @Transactional
    public void fetchAndSaveCertificates(String apiUrl) {
        CertificateCategoryApiResponse response = restTemplate.getForObject(apiUrl, CertificateCategoryApiResponse.class);

        if (response == null || response.getBody() == null || response.getBody().getItems() == null) {
            log.warn("certificate category sync skipped: empty response");
            return;
        }

        for (CertificateCategoryApiResponse.CertItemDto itemDto : response.getBody().getItems()) {
            Category category = getOrCreateCategory(itemDto.getCategoryName());
            upsertCertificate(itemDto, category);
        }
    }

    @Transactional
    public TargetCertificateSyncResult syncCertificatesByNames(List<String> requestedNames) {
        List<String> sanitizedNames = sanitizeNames(requestedNames);
        if (sanitizedNames.isEmpty()) {
            return emptyResult(List.of());
        }

        CertificateCategoryApiResponse response = restTemplate.getForObject(buildApiUri(), CertificateCategoryApiResponse.class);
        if (response == null || response.getBody() == null || response.getBody().getItems() == null) {
            log.warn("popular certificate sync skipped: empty category response");
            return emptyResult(sanitizedNames);
        }

        Set<String> normalizedRequestedNames = sanitizedNames.stream()
                .map(this::normalizeName)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<String> matchedNormalizedNames = new LinkedHashSet<>();
        Set<String> matchedNames = new LinkedHashSet<>();
        Set<String> categoryNames = new LinkedHashSet<>();
        Set<String> certIds = new LinkedHashSet<>();

        for (CertificateCategoryApiResponse.CertItemDto itemDto : response.getBody().getItems()) {
            String normalizedItemName = normalizeName(itemDto.getName());
            if (!normalizedRequestedNames.contains(normalizedItemName)) {
                continue;
            }

            Category category = getOrCreateCategory(itemDto.getCategoryName());
            Certificate certificate = upsertCertificate(itemDto, category);

            matchedNormalizedNames.add(normalizedItemName);
            matchedNames.add(certificate.getName());
            categoryNames.add(category.getName());
            certIds.add(certificate.getCertId());
        }

        List<String> missingNames = sanitizedNames.stream()
                .filter(name -> !matchedNormalizedNames.contains(normalizeName(name)))
                .toList();

        return TargetCertificateSyncResult.builder()
                .requestedNames(sanitizedNames)
                .matchedNames(List.copyOf(matchedNames))
                .missingNames(missingNames)
                .categoryNames(List.copyOf(categoryNames))
                .certIds(List.copyOf(certIds))
                .build();
    }

    private Category getOrCreateCategory(String categoryName) {
        return categoryRepository.findByName(categoryName)
                .orElseGet(() -> categoryRepository.save(
                        Category.builder()
                                .name(categoryName)
                                .sortOrder(0)
                                .build()
                ));
    }

    private Certificate upsertCertificate(CertificateCategoryApiResponse.CertItemDto itemDto, Category category) {
        return certificateRepository.findByName(itemDto.getName())
                .map(existing -> {
                    existing.updateBasicInfo(QNET_AUTHORITY, itemDto.getCertId(), category.getId());
                    return certificateRepository.save(existing);
                })
                .orElseGet(() -> certificateRepository.save(
                        Certificate.builder()
                                .name(itemDto.getName())
                                .authority(QNET_AUTHORITY)
                                .certId(itemDto.getCertId())
                                .description(null)
                                .categoryId(category.getId())
                                .build()
                ));
    }

    private URI buildApiUri() {
        return UriComponentsBuilder.fromHttpUrl(certCategoryBaseUrl)
                .queryParam("serviceKey", certCategoryKey)
                .queryParam("dataFormat", "xml")
                .queryParam("numOfRows", REQUEST_ROWS)
                .queryParam("pageNo", 1)
                .build(true)
                .toUri();
    }

    private TargetCertificateSyncResult emptyResult(List<String> requestedNames) {
        return TargetCertificateSyncResult.builder()
                .requestedNames(requestedNames)
                .matchedNames(List.of())
                .missingNames(requestedNames)
                .categoryNames(List.of())
                .certIds(List.of())
                .build();
    }

    private List<String> sanitizeNames(List<String> requestedNames) {
        if (requestedNames == null) {
            return List.of();
        }
        return requestedNames.stream()
                .filter(name -> name != null && !name.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
    }

    private String normalizeName(String name) {
        if (name == null) {
            return "";
        }
        return name.replaceAll("\\s+", "")
                .replace("(", "")
                .replace(")", "")
                .toLowerCase();
    }

    @Getter
    @Builder
    public static class TargetCertificateSyncResult {
        private List<String> requestedNames;
        private List<String> matchedNames;
        private List<String> missingNames;
        private List<String> categoryNames;
        private List<String> certIds;
    }
}
