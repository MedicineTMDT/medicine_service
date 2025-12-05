package com.ryo.identity.repository;

import com.ryo.identity.entity.MergedIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MergedIngredientRepository extends JpaRepository<MergedIngredient, Integer> {
    List<MergedIngredient> findTop10ByNameStartingWithIgnoreCase(String name);
}