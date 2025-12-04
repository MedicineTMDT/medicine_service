package com.ryo.identity.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Intake {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private LocalDateTime time;
    private String drugName;
    private Integer quantity;
    private String status;

    @ManyToOne
    @JoinColumn(name = "prescription_id")
    private Prescription prescription;
}
