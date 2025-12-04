package com.ryo.identity.repository;

import com.ryo.identity.entity.DrugInteraction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DrugInteractionRepository extends JpaRepository<DrugInteraction, Integer> {
}