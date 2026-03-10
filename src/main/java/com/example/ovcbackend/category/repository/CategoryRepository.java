package com.example.ovcbackend.category.repository;

import com.example.ovcbackend.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
