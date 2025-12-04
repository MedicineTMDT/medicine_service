package com.ryo.identity.repository;

import com.ryo.identity.entity.Drug;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DrugRepository extends JpaRepository<Drug, Integer> {
    boolean existsBySlug(String slug);
}