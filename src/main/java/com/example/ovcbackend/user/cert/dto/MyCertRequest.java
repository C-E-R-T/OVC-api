package com.example.ovcbackend.user.cert.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Schema(description = "내 자격증 등록 요청 dto")
public class MyCertRequest {
    @Schema(description = "자격증 번호", example = "00000000000A")
    private String certNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "합격일", example = "2026-03-07")
    private LocalDate passedAt;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "만료일", example = "2028-03-07")
    private LocalDate expiredAt;
}
