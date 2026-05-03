package com.ryo.request.repository;

import com.ryo.request.entity.CategoryDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryDetailRepository extends JpaRepository<CategoryDetail, Integer> {
    Page<CategoryDetail> findAllByCategory_Id(Pageable pageable, Integer categoryId);

    Page<CategoryDetail> findAllByNameContainingIgnoreCase(Pageable pageable, String name);
}
