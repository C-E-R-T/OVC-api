package com.example.ovcbackend.category.service;


import com.example.ovcbackend.category.dto.CategoryResponse;
import com.example.ovcbackend.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor

public class CategoryServiceImpl implements CategoryService{
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories(){
        // 모든 카테고리 값 반환
        return categoryRepository.findAll().stream().map(CategoryResponse::from).toList();
    }
}
