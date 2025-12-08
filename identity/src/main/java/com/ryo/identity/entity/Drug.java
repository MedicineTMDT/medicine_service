package com.ryo.identity.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "drug")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Drug {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Column(columnDefinition = "TEXT")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private String content;

    @Column(columnDefinition = "TEXT")
    private String document;

    @Column(unique = true)
    private String slug;

    // Mapping kiểu JSON cho metadata ({})
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> metadata;

    private List<String> image;

    private List<String> ingredient;

    // Mapping kiểu JSON cho info ({})
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, Object> info;

    // Quan hệ n-n với Category (đã map bên Category)
    @ManyToMany(mappedBy = "drugs",fetch = FetchType.LAZY)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Set<Category> categories = new HashSet<>();

    // Quan hệ n-n với MergedIngredient (thông qua bảng drug_ingredient_rel)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "drug_ingredient_rel",
            joinColumns = @JoinColumn(name = "drug_id"),
            inverseJoinColumns = @JoinColumn(name = "ingredient_id")
    )
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Set<MergedIngredient> mergedIngredients = new HashSet<>();
}