package com.ryo.identity.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "merged_ingredient")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MergedIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Column(name = "source_table")
    private String sourceTable;

    @Column(name = "source_id")
    private Integer sourceId;

    // Quan hệ n-n với Drug (đã map bên Drug)
    @ManyToMany(mappedBy = "mergedIngredients")
    private Set<Drug> drugs = new HashSet<>();

    // Quan hệ 1-n với Tương tác thuốc (với tư cách là hoạt chất 1)
    @OneToMany(mappedBy = "ingredient1")
    private Set<DrugInteraction> interactionsAsFirst;

    // Quan hệ 1-n với Tương tác thuốc (với tư cách là hoạt chất 2)
    @OneToMany(mappedBy = "ingredient2")
    private Set<DrugInteraction> interactionsAsSecond;
}