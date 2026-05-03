package com.ryo.request.repository;

import com.ryo.request.projection.MergedIngredientProjection;
import com.ryo.request.entity.MergedIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MergedIngredientRepository extends JpaRepository<MergedIngredient, Integer> {
    List<MergedIngredientProjection> findTop10ByNameStartingWithIgnoreCase(String name);
}