package com.ryo.identity.repository;

import com.ryo.identity.dto.response.CategoryResponse;
import com.ryo.identity.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    boolean existsBySlug(String slug);

    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);

}