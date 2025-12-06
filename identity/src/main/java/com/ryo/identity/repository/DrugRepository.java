package com.ryo.identity.repository;

import com.ryo.identity.entity.Drug;
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

    Page<Drug> findAllByCategories_Id(Pageable pageable, Integer categoryId);

    Page<Drug> findAllByNameContainingIgnoreCase(Pageable pageable, String name);

}