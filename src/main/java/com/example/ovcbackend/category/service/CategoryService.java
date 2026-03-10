package com.example.ovcbackend.category.service;

import com.example.ovcbackend.category.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllCategories();
}
