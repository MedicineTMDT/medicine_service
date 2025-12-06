package com.ryo.identity.repository;

import com.ryo.identity.entity.Drug;
import com.ryo.identity.projection.DrugProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DrugRepository extends JpaRepository<Drug, Integer> {
    boolean existsBySlug(String slug);

    Optional<Drug> findBySlug(String slug);

    Optional<Drug> findByName(String slug);

    Page<DrugProjection> getAllProjectedBy(Pageable pageable);

    Page<DrugProjection> findAllByCategories_Id(Pageable pageable, Integer categoryId);

    Page<DrugProjection> findAllByNameContainingIgnoreCase(Pageable pageable, String name);

    List<DrugProjection> findTop10ByNameStartingWithIgnoreCase(String name);

}