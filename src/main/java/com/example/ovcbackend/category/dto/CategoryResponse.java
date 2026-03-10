package com.example.ovcbackend.category.dto;

import com.example.ovcbackend.category.entity.Category;
import lombok.*;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryResponse {
    private Long id;
    private String name;

    public static CategoryResponse from(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
