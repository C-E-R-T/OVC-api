package com.example.ovcbackend.category.controller;

import com.example.ovcbackend.category.dto.CategoryResponse;
import com.example.ovcbackend.category.service.CategoryService;
import com.example.ovcbackend.global.commonResponse.OkResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Category Controller", description = "카테고리 관련 api")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<OkResponse<List<CategoryResponse>>> getAllCategories(HttpServletRequest request) {
        List<CategoryResponse> response = categoryService.getAllCategories();
        return ResponseEntity.ok(OkResponse.success(response,request.getRequestURI()));
    }

}
