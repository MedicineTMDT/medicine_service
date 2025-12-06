package com.ryo.identity.entity;

import com.ryo.identity.dto.response.DrugInteractionResponse;
import com.ryo.identity.dto.response.DrugResponse;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Prescription {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    private String name;
    private String description;

    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Intake> intakes = new ArrayList<>();

    // do sth here about prescription info :vvvx
//    private List<DrugResponse> infoList;
//    private List<DrugInteractionResponse> drugInteractionResponseList;
}
