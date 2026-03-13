package com.example.ovcbackend.user.favorite.dto;

import com.example.ovcbackend.certificate.entity.Certificate;
import com.example.ovcbackend.user.favorite.entity.Favorite;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "찜한 자격증 응답 dto")
public class FavoriteResponse {
    @Schema(description = "자격증 id", example = "1")
    private Long certId;
    private String title;
    @Schema(description = "카드 타입(APPLY/EXAM/RESULT)", example = "APPLY")
    private String type;
    @Schema(description = "시작일(yyyy-MM-dd)", example = "2026-03-10")
    private String startDate;
    @Schema(description = "종료일(yyyy-MM-dd)", example = "2026-03-15")
    private String endDate;

    public static FavoriteResponse from(Favorite favorite, String type, String startDate, String endDate) {
        // 프론트 카드 렌더링에 필요한 최소 필드만 담아 반환
        Certificate certificate = favorite.getCertificate();
        return FavoriteResponse.builder()
                .certId(certificate.getId())
                .title(certificate.getName())
                .type(type)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }
}
