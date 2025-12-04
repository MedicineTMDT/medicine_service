package com.ryo.identity.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "category")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private Integer amount;

    @Column(unique = true)
    private String slug;

    @CreationTimestamp
    private LocalDateTime created;

    // Quan hệ 1-n với CategoryDetail
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<CategoryDetail> categoryDetails;

    // Quan hệ n-n với Drug (thông qua bảng trung gian category_drug)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "category_drug",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "drug_id")
    )
    private Set<Drug> drugs = new HashSet<>();
}