package com.ryo.identity.repository;

import com.ryo.identity.entity.MergedIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MergedIngredientRepository extends JpaRepository<MergedIngredient, Integer> {
}