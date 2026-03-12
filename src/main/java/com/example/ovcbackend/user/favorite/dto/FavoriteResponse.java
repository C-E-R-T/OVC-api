package com.example.ovcbackend.user.favorite.dto;

import com.example.ovcbackend.certificate.entity.Certificate;
import com.example.ovcbackend.user.favorite.entity.Favorite;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "찜한 자격증 응답 dto")
public class FavoriteResponse {
    @Schema(description = "자격증 id", example = "1")
    private Long certId;
    private Long categoryId;
    private String name;
    private String authority;
    private String description;
    private LocalDateTime createdAt;

    public static FavoriteResponse from(Favorite favorite) {
        // 리스트 응답에서 필요한 자격증 정보만 추려서 반환
        Certificate certificate = favorite.getCertificate();

        return FavoriteResponse.builder()
                .certId(certificate.getId())
                .categoryId(certificate.getCategoryId())
                .name(certificate.getName())
                .authority(certificate.getAuthority())
                .description(certificate.getDescription())
                .createdAt(favorite.getCreatedAt())
                .build();
    }
}
