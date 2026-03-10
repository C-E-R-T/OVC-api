package com.example.ovcbackend.certificate.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Schema(description = "자격증 검색용 dto")
@Getter
@Builder
@AllArgsConstructor
public class CertSearchResponse {
    @Schema(description = "자격증 id", example = "1")
    private Long certId;
    private Long categoryId;
    private String name;
    private String authority;
    private String description;
}
