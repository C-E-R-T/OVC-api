package com.example.ovcbackend.category.repository;

import com.example.ovcbackend.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    // 이름으로 기존 카테고리 조회(중복 생성 방지)
    Optional<Category> findByName(String name);
}
