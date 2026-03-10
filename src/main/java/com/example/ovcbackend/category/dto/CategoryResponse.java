package com.example.ovcbackend.category.dto;

import com.example.ovcbackend.category.entity.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryResponse {
    @Schema(name = "카테고리 id", example = "1")
    private Long id;
    @Schema(name = "카테고리 이름", example = "정보기술")
    private String name;

    public static CategoryResponse from(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
