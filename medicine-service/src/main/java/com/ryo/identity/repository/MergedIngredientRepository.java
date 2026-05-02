package com.ryo.identity.repository;

import com.ryo.identity.entity.MergedIngredient;
import com.ryo.identity.projection.MergedIngredientProjection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MergedIngredientRepository extends JpaRepository<MergedIngredient, Integer> {
    List<MergedIngredientProjection> findTop10ByNameStartingWithIgnoreCase(String name);
}