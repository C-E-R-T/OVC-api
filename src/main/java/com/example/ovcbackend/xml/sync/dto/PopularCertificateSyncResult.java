package com.example.ovcbackend.xml.sync.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class PopularCertificateSyncResult {

    // 요청으로 들어온 원본 자격증명 목록
    private List<String> requestedNames;
    // 카테고리 API에서 실제 매칭된 자격증명
    private List<String> matchedNames;
    // 매칭에 실패한 자격증명
    private List<String> missingNames;
    // 매칭된 자격증의 카테고리명 목록
    private List<String> categoryNames;
    // 매칭된 자격증의 종목코드(jmCd) 목록
    private List<String> certIds;
    // 상세 동기화까지 성공한 종목코드 목록
    private List<String> detailSyncedCertIds;
    // 상세 동기화 스킵/실패 사유 (key: 종목코드, value: 사유 코드)
    private Map<String, String> detailSkippedReasons;
    // 일정 동기화까지 성공한 종목코드 목록
    private List<String> scheduleSyncedCertIds;
}
