package com.example.ovcbackend.xml.sync.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class PopularCertificateSyncRequest {

    @NotEmpty
    private List<String> certificateNames;
}
