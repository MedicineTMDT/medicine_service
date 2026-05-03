package com.ryo.prescription.repository;

import com.ryo.prescription.projection.MergedIngredientProjection;
import com.ryo.prescription.entity.MergedIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MergedIngredientRepository extends JpaRepository<MergedIngredient, Integer> {
    List<MergedIngredientProjection> findTop10ByNameStartingWithIgnoreCase(String name);
}