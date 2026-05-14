package com.ryo.identity.entity;

import com.ryo.identity.constant.TypeOfRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @NotBlank
    @Column(name = "title")
    String title;

    @NotBlank
    @Column(name = "content")
    String content;

    TypeOfRequest typeOfRequest;

    Boolean proceed;

    @ManyToOne
    @JoinColumn(name = "user_id")
    User user;
}


