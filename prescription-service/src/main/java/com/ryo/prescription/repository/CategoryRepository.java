package com.ryo.prescription.repository;

import com.ryo.prescription.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    boolean existsBySlug(String slug);

    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);

}