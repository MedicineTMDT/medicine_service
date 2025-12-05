package com.ryo.identity.repository;

import com.ryo.identity.entity.DrugInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DrugInteractionRepository extends JpaRepository<DrugInteraction, Integer> {
    List<DrugInteraction> findByHoatChat1Name(String name);
    List<DrugInteraction> findByHoatChat2Name(String name);
    @Query("""
        SELECT d FROM DrugInteraction d\s
        WHERE\s
            (d.hoatChat1Name = :name1 AND d.hoatChat2Name = :name2)
            OR
            (d.hoatChat1Name = :name2 AND d.hoatChat2Name = :name1)
    """)
    List<DrugInteraction> findInteractionBetween(String name1, String name2);

}