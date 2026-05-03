package com.ryo.request.repository;

import com.ryo.request.entity.DrugInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DrugInteractionRepository extends JpaRepository<DrugInteraction, Integer> {
    @Query("SELECT d FROM DrugInteraction d WHERE d.hoatChat1Name = :name OR d.hoatChat2Name = :name")
    List<DrugInteraction> findAnyInteraction(@Param("name") String name);
    @Query("""
        SELECT d FROM DrugInteraction d\s
        WHERE\s
            (d.hoatChat1Name = :name1 AND d.hoatChat2Name = :name2)
            OR
            (d.hoatChat1Name = :name2 AND d.hoatChat2Name = :name1)
    """)
    List<DrugInteraction> findInteractionBetween(String name1, String name2);

}