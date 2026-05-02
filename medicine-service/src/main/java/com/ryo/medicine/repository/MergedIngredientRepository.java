package com.ryo.medicine.repository;

import com.ryo.medicine.entity.MergedIngredient;
import com.ryo.medicine.projection.MergedIngredientProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MergedIngredientRepository extends JpaRepository<MergedIngredient, Integer> {
    List<MergedIngredientProjection> findTop10ByNameStartingWithIgnoreCase(String name);
}