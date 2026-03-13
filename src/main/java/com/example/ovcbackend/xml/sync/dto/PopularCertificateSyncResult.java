package com.example.ovcbackend.xml.sync.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PopularCertificateSyncResult {

    private List<String> requestedNames;
    private List<String> matchedNames;
    private List<String> missingNames;
    private List<String> categoryNames;
    private List<String> certIds;
    private List<String> detailSyncedCertIds;
    private List<String> scheduleSyncedCertIds;
}
